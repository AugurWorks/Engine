package com.augurworks.engine.domains

class MachineLearningModel {

	String trainingDataSourceId
	String modelId
	String predictionDataSourceId
	String batchPredictionId

	static constraints = {
		predictionDataSourceId nullable: true
		batchPredictionId nullable: true
	}

	String toString() {
		modelId
	}
}
