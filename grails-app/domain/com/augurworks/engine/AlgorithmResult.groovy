package com.augurworks.engine

class AlgorithmResult {

	Date dateCreated
	String modelId
	String batchPredictionId
	String outputUri

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dateCreated()
		modelId nullable: true
		batchPredictionId nullable: true
		outputUri nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}

	boolean isMachineLearning() {
		return modelId
	}

	boolean isComplete() {
		return this.machineLearning && this.outputUri
	}
}
