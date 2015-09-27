package com.augurworks.engine.domains

import groovy.time.TimeCategory

import org.apache.commons.lang.time.DateUtils

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

	PredictedValue getTomorrowsValue() {
		Date tomorrow = DateUtils.ceiling(new Date(), Calendar.DATE)
		Collection<PredictedValue> filtered = this.predictedValues.grep { PredictedValue value ->
			value.date == tomorrow
		}
		if (filtered.size() == 1) {
			return filtered.first()
		}
		return null
	}
}
