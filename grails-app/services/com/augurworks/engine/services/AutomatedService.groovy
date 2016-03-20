package com.augurworks.engine.services

import grails.transaction.Transactional

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.helper.SingleDataRequest

@Transactional
class AutomatedService {

	GrailsApplication grailsApplication
	MachineLearningService machineLearningService
	AlfredService alfredService
	DataRetrievalService dataRetrievalService

	void runAllDailyAlgorithms() {
		log.info 'Running all algorithms'
		AlgorithmRequest.findAllByUnit('Day').each { AlgorithmRequest req ->
			try {
				runAllAlgorithmTypes(req)
			} catch (e) {
				log.warn 'Error submitting ' + req + ': ' + e.message
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
		log.info 'Running ' + (algorithmRequest.cronAlgorithms*.name.join(', ') ?: 'no algorithms') + ' for ' + algorithmRequest + ' from a cron job'
		algorithmRequest.cronAlgorithms.each { AlgorithmType algorithmType ->
			runAlgorithm(algorithmRequest, algorithmType)
		}
	}

	void runAlgorithm(AlgorithmRequest algorithmRequest, AlgorithmType algorithmType) {
		if (algorithmType == AlgorithmType.ALFRED) {
			alfredService.createAlgorithm(algorithmRequest)
		} else if (algorithmType == AlgorithmType.MACHINE_LEARNING) {
			machineLearningService.createAlgorithm(algorithmRequest)
		}
	}

	void postProcessing(AlgorithmResult algorithmResult) {
		AlgorithmRequest algorithmRequest = algorithmResult.algorithmRequest
		RequestDataSet requestDataSet = algorithmRequest.dependentRequestDataSet
		SingleDataRequest singleDataRequest = new SingleDataRequest(
			dataSet: requestDataSet.dataSet,
			offset: requestDataSet.offset,
			startDate: algorithmResult.algorithmRequest.getStartDate(algorithmResult.dateCreated),
			endDate: algorithmResult.algorithmRequest.getEndDate(algorithmResult.dateCreated),
			unit: algorithmResult.algorithmRequest.unit,
			minOffset: requestDataSet.offset,
			maxOffset: requestDataSet.offset,
			aggregation: Aggregation.VALUE
		)
		RequestValueSet predictionActuals = dataRetrievalService.getSingleRequestValues(singleDataRequest)
		if (algorithmResult.futureValue) {
			Double actualValue
			Date futureDate = Common.calculatePredictionDate(algorithmResult.algorithmRequest.unit, predictionActuals.values.last().date, 1)
			if (futureDate == algorithmResult.futureValue.date) {
				actualValue = requestDataSet.aggregation.normalize.apply(predictionActuals.values.last().value, algorithmResult.futureValue.value)?.round(3)
			} else {
				log.warn 'Prediction actual and predicted date arrays for ' + algorithmRequest + ' do not match up'
				log.info '- Last actual date: ' + predictionActuals.values.last().date
				log.info '- Last prediction date: ' + algorithmResult.futureValue.date
			}
			if (grailsApplication.config.slack.on) {
				algorithmResult.futureValue?.sendToSlack(actualValue)
			}
		}
	}

	boolean areDataSetsCorrectlySized(Collection<Map> dataSets, int rowNumber) {
		return dataSets*.values*.size().every { it == rowNumber }
	}
}
