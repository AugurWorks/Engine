package com.augurworks.engine

class AlgorithmResult {

	String name
	Date dateCreated
	String modelId
	String evaluationId

	static hasMany = [predictedValues: PredictedValue]

	static constraints = {
		name
		dateCreated()
		modelId nullable: true
		evaluationId nullable: true
	}
}
