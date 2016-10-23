package com.augurworks.engine.jobs

import com.augurworks.engine.services.ActualValueService

class ActualValueJob {

	ActualValueService actualValueService

	def cronExpression = '0 0 2 * * ?'

	void execute() {
		actualValueService.fillOutPredictedValues()
	}
}
