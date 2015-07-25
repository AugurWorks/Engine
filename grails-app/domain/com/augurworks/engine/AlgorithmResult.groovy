package com.augurworks.engine

class AlgorithmResult {

	Date dateCreated
	boolean complete = false
	String modelId
	String batchPredictionId

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dateCreated()
		complete()
		modelId nullable: true
		batchPredictionId nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}

	boolean isMachineLearning() {
		return modelId
	}
}
