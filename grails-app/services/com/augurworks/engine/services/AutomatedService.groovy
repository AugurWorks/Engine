package com.augurworks.engine.services

import com.augurworks.engine.data.ActualValue
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.slack.SlackMessage
import grails.core.GrailsApplication
import grails.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

@Transactional
class AutomatedService {

	private static final Logger log = LoggerFactory.getLogger(AutomatedService)

	GrailsApplication grailsApplication
	MachineLearningService machineLearningService
	AlfredService alfredService
	DataRetrievalService dataRetrievalService
	ActualValueService actualValueService

	void runAllDailyAlgorithms() {
		log.info('Running all algorithms')
		AlgorithmRequest.findAllByUnit('Day').each { AlgorithmRequest req ->
			try {
				runAllAlgorithmTypes(req)
			} catch (e) {
				log.warn('Error submitting ' + req + ': ' + e.message)
			}
		}
	}

	void runAllAlgorithmTypes(AlgorithmRequest algorithmRequest) {
		AlgorithmType.values().each { AlgorithmType algorithmType ->
			runAlgorithm(algorithmRequest, algorithmType)
		}
	}

	void runCronAlgorithms(long algorithmRequestId) {
		AlgorithmRequest algorithmRequest = AlgorithmRequest.get(algorithmRequestId)
		log.info('Running ' + (algorithmRequest.cronAlgorithms*.name.join(', ') ?: 'no algorithms') + ' for ' + algorithmRequest + ' from a cron job')
		algorithmRequest.cronAlgorithms.each { AlgorithmType algorithmType ->
			try {
				runAlgorithm(algorithmRequest, algorithmType)
			} catch (Exception e) {
				String message = 'An error occurred when running a(n) ' + algorithmType.name() + ' cron algorithm for ' + algorithmRequest.name
				log.error(message, e)
				new SlackMessage(message, algorithmRequest.slackChannel ?: grailsApplication.config.augurworks.predictions.channel)
					.withBotName('Engine Predictions')
					.withColor('#444444')
					.withTitle('Error running ' + algorithmRequest.name)
					.send()
			}
		}
	}

	void runAlgorithm(AlgorithmRequest algorithmRequest, AlgorithmType algorithmType) {
		MDC.put('algorithmRequestId', algorithmRequest.id.toString())
		MDC.put('algorithmRequestName', algorithmRequest.name)
		MDC.put('algorithmType', algorithmType.name())
		log.info('Running algorithm for ' + algorithmRequest.name)
		if (algorithmType == AlgorithmType.ALFRED) {
			alfredService.createAlgorithm(algorithmRequest)
		} else if (algorithmType == AlgorithmType.MACHINE_LEARNING) {
			machineLearningService.createAlgorithm(algorithmRequest)
		}
	}

	void postProcessing(AlgorithmResult algorithmResult) {
		try {
			Optional<ActualValue> actualValue = actualValueService.getActual(algorithmResult)
			if (actualValue.isPresent()) {
				algorithmResult.actualValue = actualValue.get().predictedValue
				algorithmResult.predictedDate = actualValue.get().date
				algorithmResult.save()
				if (grailsApplication.config.slack.webhook) {
					algorithmResult.futureValue?.sendToSlack(actualValue.get())
				}
			}
		} catch (e) {
			log.error('Post processing failed', e)
		}
	}

	boolean areDataSetsCorrectlySized(Collection<Map> dataSets, int rowNumber) {
		return dataSets*.values*.size().every { it == rowNumber }
	}
}
