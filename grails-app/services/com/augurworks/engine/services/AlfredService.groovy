package com.augurworks.engine.services

import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.TrainingStat
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlfredEnvironment
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.Global
import com.augurworks.engine.messaging.TrainingConfig
import com.augurworks.engine.messaging.TrainingMessage
import com.augurworks.engine.messaging.TrainingMessageV1
import com.augurworks.engine.messaging.TrainingMessageV2
import com.augurworks.engine.model.RequestValueSet
import com.fasterxml.jackson.databind.ObjectMapper
import grails.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

@Transactional
class AlfredService {

	private static final Logger log = LoggerFactory.getLogger(AlfredService)

	private final ObjectMapper mapper = new ObjectMapper()

	DataRetrievalService dataRetrievalService
	AutomatedService automatedService
	MessagingService messagingService
	AutoScalingService autoScalingService
	AwsService awsService

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String netId = UUID.randomUUID().toString()
		MDC.put('netId', netId)
		log.info('Created Alfred algorithm run for ' + algorithmRequest.name)
		String postBody = constructPostBody(algorithmRequest)
		TrainingMessage message = new TrainingMessageV1(netId).withData(postBody)
		messagingService.sendTrainingMessage(message, algorithmRequest.alfredEnvironment == AlfredEnvironment.LAMBDA)
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

		try {
			awsService.uploadToS3('alfred', 'alfred-' + netId + '.json', mapper.writeValueAsString(message))
		} catch (Exception e) {
			log.error('Error uploading Alfred data to S3', e)
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
		 throw new AugurWorksException('Dependant data set not sized correctly compared to independent data sets')
		 }*/
		Collection<String> lines = ['net ' + (dataSets.size() - 1) + ',5', 'train 1,700,0.1,700,0.000001', 'TITLES ' + dataSets.tail()*.name.join(',')
		]+ (0..(rowNumber - 1)).collect { int row ->
			// TO-DO: Will not work for predictions of more than one period
			Date date = dataSets*.values.first()[row]?.date ?: Common.calculatePredictionDate(algorithmRequest.unit, dataSets*.values.first()[row - 1].date, 1)
			return date.format(Global.ALFRED_DATE_FORMAT) + ' ' + (dataSets.first().values[row]?.value ?: 'NULL') + ' ' + dataSets.tail()*.values.collect { it[row].value }.join(',')
		}
		return lines.join('\n')
	}

    TrainingMessageV2 constructTrainingMessage(String id, AlgorithmRequest algorithmRequest) {
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
        TrainingConfig trainingConfig = new TrainingConfig().withDepth(5).withIterations(700).withLearningRate(0.1).withTitles(dataSets.tail()*.name)
        List<List<String>> data = (0..(rowNumber - 1)).collect { int rowNum ->
            // TO-DO: Will not work for predictions of more than one period
            Date date = dataSets*.values.first()[rowNum]?.date ?: Common.calculatePredictionDate(algorithmRequest.unit, dataSets*.values.first()[rowNum - 1].date, 1)
            List<String> row = new ArrayList<>()
            row.add(date.format(Global.ALFRED_DATE_FORMAT))
            row.add(dataSets.first().values[rowNum]?.value.toString() ?: 'NULL')
            row.addAll(dataSets.tail()*.values.collect { it[rowNum].value.toString() })
            return row
        }
        return new TrainingMessageV2(id).withTrainingConfig(trainingConfig).withData(data)
    }

	void processResult(TrainingMessage message) {
		MDC.put('netId', message.getNetId())
		AlgorithmResult algorithmResult = AlgorithmResult.findByAlfredModelId(message.getNetId())

		if (algorithmResult == null) {
			throw new AugurWorksException('Algorithm ' + message.getNetId() + ' doesn\'t exist')
		}

		MDC.put('algorithmRequestId', algorithmResult.algorithmRequest.id.toString())
		MDC.put('algorithmResultId', algorithmResult.id.toString())

		log.debug('Received results message from net ' + algorithmResult.alfredModelId)

		algorithmResult.complete = true
		message.processResults(algorithmResult)
		algorithmResult.save(flush: true)

		List<TrainingStat> trainingStats = message.getTrainingStats()
		trainingStats.each { TrainingStat trainingStat ->
			trainingStat.save()
		}

		automatedService.postProcessing(algorithmResult)

		log.info('Finished processing message from net ' + algorithmResult.alfredModelId)
		
		MDC.remove('netId')
		MDC.remove('algorithmRequestId')
		MDC.remove('algorithmResultId')
	}
}
