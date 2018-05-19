package com.augurworks.engine.jobs

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.services.AutomatedService
import grails.util.Holders
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AlgorithmRequestJob implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(AlgorithmRequestJob)

	AutomatedService automatedService = Holders.grailsApplication.mainContext.getBean 'automatedService'

	long algorithmRequestId

	AlgorithmRequestJob(long algorithmRequestId) {
		this.algorithmRequestId = algorithmRequestId
	}

	@Override
	void run() {
		log.debug('Running cron from algorithm request ' + algorithmRequestId)
		AlgorithmRequest.withTransaction { status ->
			automatedService.runCronAlgorithms(algorithmRequestId)
		}
	}
}
