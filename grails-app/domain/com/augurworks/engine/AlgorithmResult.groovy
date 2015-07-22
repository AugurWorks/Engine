package com.augurworks.engine

class AlgorithmResult {

	String name
	Date dateCreated
	String modelId
	String modelStatus
	String evaluationId

	static hasMany = [predictedValues: PredictedValue]

	static constraints = {
		name
		dateCreated()
		modelId nullable: true
		modelStatus nullable: true
		evaluationId nullable: true
	}

	boolean isMachineLearning() {
		return modelId
	}

	boolean isComplete() {
		Collection<String> completeStatuses = ['FAILED', 'COMPLETED', 'DELETED']
		return this.machineLearning && modelStatus in completeStatuses
	}
}
