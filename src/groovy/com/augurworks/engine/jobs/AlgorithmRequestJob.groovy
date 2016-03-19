package com.augurworks.engine.jobs

import grails.util.Holders

import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.services.AutomatedService

class AlgorithmRequestJob implements Runnable {

	AutomatedService automatedService = Holders.grailsApplication.mainContext.getBean 'automatedService'

	long algorithmRequestId

	AlgorithmRequestJob() { }

	AlgorithmRequestJob(long algorithmRequestId) {
		this.algorithmRequestId = algorithmRequestId
	}

	@Override
	void run() {
		automatedService.runAlgorithm(algorithmRequestId, AlgorithmType.ALFRED)
	}
}
