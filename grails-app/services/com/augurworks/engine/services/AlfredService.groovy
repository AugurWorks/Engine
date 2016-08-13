package com.augurworks.engine.services

import grails.transaction.Transactional

import org.slf4j.MDC

import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.domains.TrainingStat
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlfredEnvironment
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.Global
import com.augurworks.engine.messaging.TrainingMessage
import com.augurworks.engine.model.RequestValueSet

@Transactional
class AlfredService {

	DataRetrievalService dataRetrievalService
	AutomatedService automatedService
	MessagingService messagingService
	AutoScalingService autoScalingService

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String netId = UUID.randomUUID().toString()
		MDC.put('netId', netId)
		log.info('Created Alfred algorithm run for ' + algorithmRequest.name)
		String postBody = constructPostBody(algorithmRequest)
		messagingService.sendTrainingMessage(new TrainingMessage(netId, postBody), algorithmRequest.alfredEnvironment == AlfredEnvironment.LAMBDA)
		AlgorithmResult algorithmResult = new AlgorithmResult([
			algorithmRequest: algorithmRequest,
			startDate: algorithmRequest.startDate,
			endDate: algorithmRequest.endDate,
			alfredModelId: netId,
			modelType: AlgorithmType.ALFRED
		])
		algorithmResult.save()
		MDC.remove('netId')

		if (algorithmRequest.alfredEnvironment == AlfredEnvironment.DOCKER) {
			autoScalingService.checkSpinUp()
		}
	}

	String constructPostBody(AlgorithmRequest algorithmRequest) {
		SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest, prediction: true)
		Collection<RequestValueSet> dataSets = dataRetrievalService.smartSpline(splineRequest).sort { RequestValueSet requestValueSetA, RequestValueSet requestValueSetB ->
			Collection<String> dependantFields = algorithmRequest.dependantSymbol.split(' - ')
			return (requestValueSetB.name == dependantFields[0] && requestValueSetB.dataType.name() == dependantFields[1]) <=> (requestValueSetA.name == dependantFields[0] && requestValueSetA.dataType.name() == dependantFields[1]) ?: requestValueSetA.name <=> requestValueSetB.name
		}
		int rowNumber = dataSets*.values*.size().max()
		if (!automatedService.areDataSetsCorrectlySized(dataSets.tail(), rowNumber)) {
			String dataSetLengths = dataSets.tail().collect { RequestValueSet dataSet ->
				return dataSet.name + ' - ' + dataSet.offset + ': ' + dataSet.values.size() + ' dates'
			}.join(', ')
			throw new AugurWorksException('Request datasets aren\'t all the same length (' + dataSetLengths + ')')
		}
		/*if (dataSets.first().values.size() != rowNumber - 1) {
		 throw new AugurWorksException('Dependant data set not sized correctly compared to independant data sets')
		 }*/
		Collection<String> lines = ['net ' + (dataSets.size() - 1) + ',5', 'train 1,2500,0.1,2500,0.01', 'TITLES ' + dataSets.tail()*.name.join(',')
		]+ (0..(rowNumber - 1)).collect { int row ->
			// TO-DO: Will not work for predictions of more than one period
			Date date = dataSets*.values.first()[row]?.date ?: Common.calculatePredictionDate(algorithmRequest.unit, dataSets*.values.first()[row - 1].date, 1)
			return date.format(Global.ALFRED_DATE_FORMAT) + ' ' + (dataSets.first().values[row]?.value ?: 'NULL') + ' ' + dataSets.tail()*.values.collect { it[row].value }.join(',')
		}
		return lines.join('\n')
	}

	void processResult(TrainingMessage message) {
		MDC.put('netId', message.getNetId())
		AlgorithmResult algorithmResult = AlgorithmResult.findByAlfredModelId(message.getNetId())

		if (algorithmResult == null) {
			throw new AugurWorksException('Algorithm ' + message.getNetId() + ' doesn\' exist')
		}

		MDC.put('algorithmRequestId', algorithmResult.algorithmRequest.id.toString())
		MDC.put('algorithmResultId', algorithmResult.id.toString())

		log.debug 'Received results message from net ' + algorithmResult.alfredModelId

		algorithmResult.complete = true
		processResponse(algorithmResult, message.getData())
		algorithmResult.save(flush: true)

		List<TrainingStat> trainingStats = message.getTrainingStats()
		trainingStats.each { TrainingStat trainingStat ->
			trainingStat.save()
		}

		automatedService.postProcessing(algorithmResult)

		log.info 'Finished processing message from net ' + algorithmResult.alfredModelId
		
		MDC.remove('netId')
		MDC.remove('algorithmRequestId')
		MDC.remove('algorithmResultId')
	}

	void processResponse(AlgorithmResult algorithmResult, String text) {
		Collection<String> lines = text.split('\n')
		lines[0..(lines.size() - 1)].each { String line ->
			Collection<String> cols = line.split(' ')
			new PredictedValue(
				date: Date.parse(Global.ALFRED_DATE_FORMAT, cols[0]),
				value: cols[2].toDouble(),
				algorithmResult: algorithmResult
			).save()
		}
	}
}
