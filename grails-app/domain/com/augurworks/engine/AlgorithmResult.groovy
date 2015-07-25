package com.augurworks.engine

class AlgorithmResult {

	Date dateCreated
	String modelId
	String modelStatus
	String evaluationId

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dateCreated()
		modelId nullable: true
		modelStatus nullable: true
		evaluationId nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}

	boolean isMachineLearning() {
		return modelId
	}

	boolean isComplete() {
		Collection<String> completeStatuses = ['FAILED', 'COMPLETED', 'DELETED']
		return this.machineLearning && modelStatus in completeStatuses
	}
}
