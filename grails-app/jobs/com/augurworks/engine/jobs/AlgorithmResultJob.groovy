package com.augurworks.engine.jobs

import com.augurworks.engine.AlgorithmResult
import com.augurworks.engine.MachineLearningService

class AlgorithmResultJob {

	MachineLearningService machineLearningService

	static triggers = {
		simple startDelay: 30000, repeatInterval: 30000
	}

	void execute() {
		machineLearningService.checkIncompleteAlgorithms()
	}
}
