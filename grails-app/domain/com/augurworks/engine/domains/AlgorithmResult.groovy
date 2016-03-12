package com.augurworks.engine.domains

import com.augurworks.engine.helper.AlgorithmType

class AlgorithmResult {

	Date dateCreated
	Date startDate
	Date endDate
	boolean complete = false
	AlgorithmType modelType
	MachineLearningModel machineLearningModel
	String alfredModelId

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dateCreated()
		startDate()
		endDate()
		complete()
		modelType()
		machineLearningModel nullable: true
		alfredModelId nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}

	PredictedValue getFutureValue() {
		Date endDate = this.algorithmRequest.getEndDate(this.dateCreated)
		Collection<PredictedValue> filtered = this.predictedValues.sort { it.date }.grep { PredictedValue value ->
			value.date > endDate
		}
		if (filtered.size() >= 1) {
			return filtered.first()
		}
		return null
	}

	String toString() {
		algorithmRequest.toString() + ': ' + dateCreated.format('MM/dd/yyyy HH:mm')
	}
}
