package com.augurworks.engine.jobs

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.services.MachineLearningService

class AlgorithmKickOffJob {

	MachineLearningService machineLearningService

	static triggers = {
		cron cronExpression: '0 0 5 ? * MON-FRI'
	}

	def execute() {
		machineLearningService.runAllAlgorithms()
	}
}
