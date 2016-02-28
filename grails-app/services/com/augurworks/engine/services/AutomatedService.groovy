package com.augurworks.engine.services

import grails.transaction.Transactional

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.helper.Global

@Transactional
class AutomatedService {

	MachineLearningService machineLearningService
	AlfredService alfredService

	void runAlgorithm(AlgorithmRequest algorithmRequest, String type) {
		if (type == Global.MODEL_TYPES[0]) {
			machineLearningService.createAlgorithm(algorithmRequest)
		} else if (type == Global.MODEL_TYPES[1]) {
			alfredService.createAlgorithm(algorithmRequest)
		}
	}
}
