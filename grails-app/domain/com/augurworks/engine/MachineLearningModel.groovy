package com.augurworks.engine

class MachineLearningModel {

	String trainingDataSourceId
	String modelId
	String predictionDataSourceId
	String batchPredictionId

	static constraints = {
		trainingDataSourceId()
		modelId()
		predictionDataSourceId nullable: true
		batchPredictionId nullable: true
	}
}
