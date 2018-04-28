package com.augurworks.engine.services

import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.TrainingStat
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlfredEnvironment
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.TradingHours
import com.augurworks.engine.instrumentation.Instrumentation
import com.augurworks.engine.messaging.TrainingMessage
import com.augurworks.engine.model.RequestValueSet
import com.fasterxml.jackson.databind.ObjectMapper
import com.timgroup.statsd.StatsDClient
import grails.core.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Service

@Service
class AlfredService {

	private static final Logger log = LoggerFactory.getLogger(AlfredService)

	private final ObjectMapper mapper = new ObjectMapper()

	private final StatsDClient statsdClient = Instrumentation.statsdClient

	GrailsApplication grailsApplication
	DataRetrievalService dataRetrievalService
	AutomatedService automatedService
	MessagingService messagingService
	AutoScalingService autoScalingService
	AwsService awsService

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String netId = UUID.randomUUID().toString()
		MDC.put('netId', netId)
		log.info('Created Alfred algorithm run for ' + algorithmRequest.name)
        List<RequestValueSet> dataSets = getDataSets(algorithmRequest)
		Class<TrainingMessage> trainingMessageVersion = grailsApplication.config.messaging.version
		TrainingMessage message = trainingMessageVersion.constructTrainingMessage(netId, algorithmRequest, dataSets)
		messagingService.sendTrainingMessage(message, algorithmRequest.alfredEnvironment == AlfredEnvironment.LAMBDA)
		AlgorithmResult algorithmResult = new AlgorithmResult([
			adjustedDateCreated: TradingHours.floorAnyPeriod(new Date(), algorithmRequest.unit.minutes),
			algorithmRequest: algorithmRequest,
			startDate: algorithmRequest.startDate,
			endDate: algorithmRequest.endDate,
			alfredModelId: netId,
			modelType: AlgorithmType.ALFRED
		])
		algorithmResult.save()
		if (algorithmResult.hasErrors()) {
			log.error('Error saving AlgorithmResult: ' + algorithmResult.errors)
		}
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

    private List<RequestValueSet> getDataSets(AlgorithmRequest algorithmRequest) {
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
        return dataSets
    }

	void processResult(TrainingMessage message) {
		MDC.put('netId', message.getNetId())
		statsdClient.increment('count.alfred.processed')
		AlgorithmResult algorithmResult = AlgorithmResult.findByAlfredModelId(message.getNetId())

		if (algorithmResult == null) {
			throw new AugurWorksException('Algorithm ' + message.getNetId() + ' doesn\'t exist')
		}

		MDC.put('algorithmRequestId', algorithmResult.algorithmRequest.id.toString())
		MDC.put('algorithmResultId', algorithmResult.id.toString())

		if (algorithmResult.getComplete()) {
			log.warn('Algorithm result has already been processed, skipping result')
			MDC.clear()
			return
		}

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
		MDC.clear()
	}
}
