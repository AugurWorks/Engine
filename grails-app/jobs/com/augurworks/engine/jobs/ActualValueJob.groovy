package com.augurworks.engine.jobs

import com.augurworks.engine.services.ActualValueService

class ActualValueJob {

	ActualValueService actualValueService

	static triggers = {
		cron name: 'nightly', startDelay: 10000, cronExpression: '0 0 2 * * ?'
	}

	void execute() {
		actualValueService.fillOutPredictedValues()
	}
}
