package com.augurworks.engine

class AlgorithmResult {

	static hasMany = [predictedValues: PredictedValue]

	static constraints = {
		
	}

	static mapping = {
		autoTimestamp true
	}
}
