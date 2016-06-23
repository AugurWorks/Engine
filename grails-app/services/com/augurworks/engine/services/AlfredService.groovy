package com.augurworks.engine.services

import grails.transaction.Transactional

import org.slf4j.MDC

import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.exceptions.AugurWorksException
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

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String netId = UUID.randomUUID().toString()
		MDC.put('netId', netId)
		log.info('Created Alfred algorithm run for ' + algorithmRequest.name)
		String postBody = constructPostBody(algorithmRequest)
		messagingService.sendTrainingMessage(new TrainingMessage(netId, postBody))
		AlgorithmResult algorithmResult = new AlgorithmResult([
			algorithmRequest: algorithmRequest,
			startDate: algorithmRequest.startDate,
			endDate: algorithmRequest.endDate,
			alfredModelId: netId,
			modelType: AlgorithmType.ALFRED
		])
		algorithmResult.save()
		MDC.remove('netId')
	}

	String constructPostBody(AlgorithmRequest algorithmRequest) {
		SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest, prediction: true)
		Collection<RequestValueSet> dataSets = dataRetrievalService.smartSpline(splineRequest).sort { RequestValueSet requestValueSetA, RequestValueSet requestValueSetB ->
			return (requestValueSetB.name == algorithmRequest.dependantSymbol) <=> (requestValueSetA.name == algorithmRequest.dependantSymbol) ?: requestValueSetA.name <=> requestValueSetB.name
		}
		int rowNumber = dataSets*.values*.size().max()
		if (!automatedService.areDataSetsCorrectlySized(dataSets.tail(), rowNumber)) {
			throw new AugurWorksException('Request datasets aren\'t all the same length')
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
		AlgorithmResult algorithmResult = AlgorithmResult.findByAlfredModelId(message.getNetId())

		MDC.put('algorithmRequestId', algorithmResult.algorithmRequest.id.toString())
		MDC.put('algorithmResultId', algorithmResult.id.toString())

		log.debug 'Received results message from net ' + algorithmResult.alfredModelId

		algorithmResult.complete = true
		processResponse(algorithmResult, message.getData())
		algorithmResult.save(flush: true)
		automatedService.postProcessing(algorithmResult)

		log.info 'Finished processing message from net ' + algorithmResult.alfredModelId

		MDC.remove('algorithmRequestId')
		MDC.remove('algorithmResultId')
	}

	void processResponse(AlgorithmResult algorithmResult, String text) {
		Collection<String> lines = text.split('\n')
		lines[4..(lines.size() - 1)].each { String line ->
			Collection<String> cols = line.split(' ')
			new PredictedValue(
				date: Date.parse(Global.ALFRED_DATE_FORMAT, cols[0]),
				value: cols[2].toDouble(),
				algorithmResult: algorithmResult
			).save()
		}
	}
}
