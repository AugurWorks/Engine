package com.augurworks.engine.services

import com.augurworks.engine.data.ActualValue
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.exceptions.DataException
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
	ActualValueService actualValueService
	ProductService productService

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
				runAlgorithmWithRerun(algorithmRequest, algorithmType, grailsApplication.config.retry.count)
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

	void runAlgorithmWithRerun(AlgorithmRequest algorithmRequest, AlgorithmType algorithmType, int triesRemaining) {
		try {
			runAlgorithm(algorithmRequest, algorithmType)
		} catch (DataException e) {
			String message = 'An error occurred when running a(n) ' + algorithmType.name() + ' cron algorithm for ' + algorithmRequest.name + '. Rerunning in ' + grailsApplication.config.retry.seconds + ' seconds'
			log.warn(message, e)
			new SlackMessage(message, algorithmRequest.slackChannel ?: grailsApplication.config.augurworks.predictions.channel)
					.withBotName('Engine Predictions')
					.withColor('warning')
					.withTitle('Error running ' + algorithmRequest.name)
					.send()
			sleep(grailsApplication.config.retry.seconds * 1000)
			if (triesRemaining == 1) {
				runAlgorithm(algorithmRequest, algorithmType)
			} else {
				runAlgorithmWithRerun(algorithmRequest, algorithmType, triesRemaining - 1)
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
			List<AlgorithmResult> previousAlgorithmResult = AlgorithmResult.findAllByAlgorithmRequest(algorithmResult.algorithmRequest, [
					max: 1, sort: 'dateCreated', order: 'desc', offset: 1
			])
			Optional<ActualValue> actualValue = actualValueService.getActual(algorithmResult)
			Optional<ActualValue> previousActualValue = previousAlgorithmResult.size() != 1 ? Optional.empty() : actualValueService.getActual(previousAlgorithmResult.get(0))
			if (actualValue.isPresent()) {
				algorithmResult.actualValue = actualValue.get().predictedValue
				algorithmResult.predictedDate = actualValue.get().date
				algorithmResult.previousAlgorithmResult = previousAlgorithmResult.size() != 1 ? null : previousAlgorithmResult.get(0)
				algorithmResult.save()
				if (grailsApplication.config.slack.webhook) {
					algorithmResult.futureValue?.sendToSlack(actualValue.get(), previousActualValue)
				}
				algorithmResult.futureValue?.sendToSns(actualValue.get(), previousActualValue)
			}
			if (algorithmResult.algorithmRequest.product) {
				productService.createProductResult(algorithmResult)
			}
		} catch (e) {
			log.error('Post processing failed', e)
		}
	}

	boolean areDataSetsCorrectlySized(Collection<Map> dataSets, int rowNumber) {
		return dataSets*.values*.size().every { it == rowNumber }
	}
}
