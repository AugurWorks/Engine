package com.augurworks.engine.domains

import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.TradingHours
import com.augurworks.engine.model.prediction.PredictionRuleResult
import com.augurworks.engine.model.prediction.RuleEvaluationAction
import com.augurworks.engine.model.prediction.RuleEvaluationResult

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

	PredictionRuleResult evaluateRules() {
		AlgorithmRequest algorithmRequest = this.algorithmRequest
		if (!this.actualValue || !this?.previousAlgorithmResult?.actualValue) {
			return new PredictionRuleResult('Current or previous run data is missing')
		}
		if (!algorithmRequest.upperPercentThreshold || !algorithmRequest.lowerPercentThreshold || !algorithmRequest.upperPredictionPercentThreshold || !algorithmRequest.lowerPredictionPercentThreshold) {
			return new PredictionRuleResult('All prediction rules must be set')
		}
		Double predictedDifference = this.predictedDifference
		Double predictedChange = this.predictionChange.get()

		List<RuleEvaluationResult> ruleEvaluations = []
		if (predictedDifference > algorithmRequest.upperPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.BUY, 'Predicted percent change of ' + predictedDifference + '% is more than the upper threshold of ' + algorithmRequest.upperPercentThreshold + '%'))
		} else if (predictedDifference < algorithmRequest.lowerPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.SELL, 'Predicted percent change of ' + predictedDifference + '% is less than the lower threshold of ' + algorithmRequest.lowerPercentThreshold + '%'))
		} else {
			ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.HOLD, 'Predicted percent change of ' + predictedDifference + '% is between lower the threshold of ' + algorithmRequest.lowerPercentThreshold + '% and upper threshold of ' + algorithmRequest.upperPercentThreshold))
		}

		if (predictedChange > algorithmRequest.upperPredictionPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.BUY, 'Change in prediction of ' + predictedChange + ' is more than the upper threshold of ' + algorithmRequest.upperPredictionPercentThreshold))
		} else if (predictedChange < algorithmRequest.lowerPredictionPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.SELL, 'Change in prediction of ' + predictedChange + ' is less than the lower threshold of ' + algorithmRequest.lowerPredictionPercentThreshold))
		} else {
			ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.HOLD, 'Change in prediction of ' + predictedChange + ' is between lower the threshold of ' + algorithmRequest.lowerPredictionPercentThreshold + ' and upper threshold of ' + algorithmRequest.upperPredictionPercentThreshold))
		}
		return new PredictionRuleResult(ruleEvaluations*.message.join('\n'), ruleEvaluations*.action.unique().size() == 1 ? ruleEvaluations*.action.unique().first() : RuleEvaluationAction.HOLD)
	}
}
