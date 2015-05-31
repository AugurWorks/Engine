package com.augurworks.engine

class AlgorithmResult {

	Date dateCreated

	static hasMany = [predictedValues: PredictedValue]

	static constraints = {
		
	}
}
