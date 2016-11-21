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
		machineLearningModel nullable: true
		alfredModelId nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}

	List<PredictedValue> getFutureValues() {
		Date endDate = this.algorithmRequest.getEndDate(this.dateCreated)
		return this.predictedValues.sort { it.date }.grep { PredictedValue value ->
			value.date > endDate
		}
	}

	PredictedValue getFutureValue() {
		Collection<PredictedValue> filtered = getFutureValues()
		if (filtered.size() >= 1) {
			return filtered.first()
		}
		return null
	}

	String toString() {
		algorithmRequest.toString() + ': ' + dateCreated.format('MM/dd/yyyy HH:mm')
	}
}
