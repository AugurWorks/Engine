package com.augurworks.engine.jobs

import com.augurworks.engine.services.AutomatedService

class AlgorithmKickOffJob {

	AutomatedService automatedService

	static triggers = {
		cron cronExpression: '0 0 3 ? * *'
	}

	def execute() {
		automatedService.runAllDailyAlgorithms()
	}
}
