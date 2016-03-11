package com.augurworks.engine.services

import grails.transaction.Transactional

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.helper.Global

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
		Global.MODEL_TYPES.each { String type ->
			runAlgorithm(algorithmRequest, type)
		}
	}

	void runAlgorithm(AlgorithmRequest algorithmRequest, String type) {
		if (type == Global.MODEL_TYPES[0]) {
			alfredService.createAlgorithm(algorithmRequest)
		} else if (type == Global.MODEL_TYPES[1]) {
			machineLearningService.createAlgorithm(algorithmRequest)
		}
	}
}
