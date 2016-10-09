package com.augurworks.engine.jobs

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.AlfredEnvironment
import com.augurworks.engine.services.AutoScalingService

class AutoScalingGroupCheckJob {

	AutoScalingService autoScalingService

	static triggers = {
		simple startDelay: 300000, repeatInterval: 300000
	}

	void execute() {
		List<AlgorithmResult> running = AlgorithmResult.findAllByComplete(false).grep { AlgorithmResult result ->
			return result.algorithmRequest.alfredEnvironment == AlfredEnvironment.DOCKER
		}
		if (running.size() != 0) {
			autoScalingService.checkSpinUp()
		}
	}
}
