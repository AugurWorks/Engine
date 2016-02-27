package com.augurworks.engine.domains

import com.augurworks.engine.helper.Global

class AlgorithmResult {

	Date dateCreated
	Date startDate
	Date endDate
	boolean complete = false
	String modelType
	MachineLearningModel machineLearningModel
	String alfredModelId

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dateCreated()
		startDate()
		endDate()
		complete()
		modelType inList: Global.MODEL_TYPES
		machineLearningModel nullable: true
		alfredModelId nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}

	PredictedValue getFutureValue() {
		Date endDate = this.algorithmRequest.getEndDate(this.startDate)
		Collection<PredictedValue> filtered = this.predictedValues.sort { it.date }.grep { PredictedValue value ->
			value.date > endDate
		}
		if (filtered.size() >= 1) {
			return filtered.first()
		}
		return null
	}
}
