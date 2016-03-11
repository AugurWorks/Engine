package com.augurworks.engine.services

import grails.transaction.Transactional

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.helper.AlgorithmType

@Transactional
class AutomatedService {

	MachineLearningService machineLearningService
	AlfredService alfredService

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

	void runAlgorithm(AlgorithmRequest algorithmRequest, AlgorithmType algorithmType) {
		if (algorithmType == AlgorithmType.ALFRED) {
			alfredService.createAlgorithm(algorithmRequest)
		} else if (algorithmType == AlgorithmType.MACHINE_LEARNING) {
			machineLearningService.createAlgorithm(algorithmRequest)
		}
	}
}
