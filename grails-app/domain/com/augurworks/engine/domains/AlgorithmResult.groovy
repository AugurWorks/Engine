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

	PredictedValue getFutureValue() {
		Date endDate = this.algorithmRequest.endDate
		Collection<PredictedValue> filtered = this.predictedValues.sort { it.date }.grep { PredictedValue value ->
			value.date > endDate
		}
		if (filtered.size() >= 1) {
			return filtered.first()
		}
		return null
	}
}
