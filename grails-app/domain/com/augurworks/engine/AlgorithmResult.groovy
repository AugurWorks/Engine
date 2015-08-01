package com.augurworks.engine

class AlgorithmResult {

	Date dateCreated
	boolean complete = false
	MachineLearningModel machineLearningModel

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dateCreated()
		complete()
		machineLearningModel nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}
}
