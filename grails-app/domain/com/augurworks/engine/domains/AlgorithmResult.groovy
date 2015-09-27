package com.augurworks.engine.domains

class AlgorithmResult {

	Date dateCreated
	Date startDate
	Date endDate
	boolean complete = false
	MachineLearningModel machineLearningModel

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dateCreated()
		startDate()
		endDate()
		complete()
		machineLearningModel nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}
}
