package com.augurworks.engine.jobs

import com.augurworks.engine.services.ActualValueService

class ActualValueJob {

	ActualValueService actualValueService

	static triggers = {
		cron name: 'welcomeTrigger', startDelay: 10000, cronExpression: '0 0 2 * * ?'
	}

	void execute() {
		try {
			actualValueService.fillOutPredictedValues()
		} catch (Exception e) {
			log.error('Error running the actual value service job', e)
		}
	}
}
