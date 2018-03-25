package com.augurworks.engine.domains

import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.TradingHours

class AlgorithmResult {

	Date dateCreated = new Date()
	Date startDate
	Date endDate
	boolean complete = false
	AlgorithmType modelType
	MachineLearningModel machineLearningModel
	String alfredModelId
	Date predictedDate
	Double actualValue

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	AlgorithmRequest algorithmRequest
	List<PredictedValue> predictedValues

	static constraints = {
		machineLearningModel nullable: true
		alfredModelId nullable: true
		actualValue nullable: true
		predictedDate nullable: true
	}

	static mapping = {
		predictedValues cascade: 'all-delete-orphan'
	}

	List<PredictedValue> getFutureValues() {
		Date endDate = TradingHours.floorPeriod(this.algorithmRequest.getEndDate(this.dateCreated), this.algorithmRequest.unit.interval)
		return this.predictedValues.sort { it.date }.grep { PredictedValue value ->
			value.date > endDate
		}
	}

	PredictedValue getFutureValue() {
		Collection<PredictedValue> filtered = getFutureValues()
		if (filtered.size() >= 1) {
			return filtered.last()
		}
		return null
	}

	String toString() {
		algorithmRequest.toString() + ': ' + dateCreated.format('MM/dd/yyyy HH:mm')
	}
}
