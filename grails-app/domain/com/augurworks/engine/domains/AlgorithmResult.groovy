package com.augurworks.engine.domains

import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.TradingHours

class AlgorithmResult {

	Date dateCreated = new Date()
	Date adjustedDateCreated
	Date startDate
	Date endDate
	boolean complete = false
	AlgorithmType modelType
	MachineLearningModel machineLearningModel
	String alfredModelId
	Date predictedDate
    // Unaggregated prediction value
    Double actualValue
    // Unaggregated prediction minus previous unagreggated actual
    Double predictedDifference

    AlgorithmResult previousAlgorithmResult

	static hasMany = [predictedValues: PredictedValue]

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	AlgorithmRequest algorithmRequest
	List<PredictedValue> predictedValues

	static constraints = {
		adjustedDateCreated nullable: true
		machineLearningModel nullable: true
		alfredModelId nullable: true
		predictedDate nullable: true
		actualValue nullable: true
		predictedDifference nullable: true
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

    Optional<Double> getPredictionChange() {
        if (!previousAlgorithmResult) {
            return Optional.empty()
        }
        return Optional.of(actualValue - previousAlgorithmResult.actualValue)
    }

    Optional<Double> getPredictionPercentChange() {
        if (!previousAlgorithmResult) {
            return Optional.empty()
        }
        return Optional.of(100 * (actualValue - previousAlgorithmResult.actualValue) / previousAlgorithmResult.actualValue)
    }
}
