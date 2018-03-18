package com.augurworks.engine.model.prediction

import com.augurworks.engine.data.ActualValue
import com.augurworks.engine.domains.AlgorithmRequest

class PredictionRuleResult {

    public String message
    public String action

    private PredictionRuleResult(String message) {
        this(message, null)
    }

    private PredictionRuleResult(String message, RuleEvaluationAction action) {
        this.message = message
        this.action = action
    }

    static PredictionRuleResult create(AlgorithmRequest algorithmRequest, ActualValue actualValue, Optional<ActualValue> previousActualValue = Optional.empty()) {
        if (actualValue == null || !previousActualValue.isPresent()) {
            return new PredictionRuleResult('Current or previous run data is missing')
        }
        if (algorithmRequest.upperPercentThreshold == null || algorithmRequest.lowerPercentThreshold == null || algorithmRequest.upperPredictionPercentThreshold == null || algorithmRequest.lowerPredictionPercentThreshold == null) {
            return new PredictionRuleResult('All prediction rules must be set')
        }
        Double changePercent = (100 * (actualValue.getPredictedValue() - actualValue.getCurrentValue()) / actualValue.getCurrentValue()).round(3)
        Double predictedChange = actualValue.predictedValue - previousActualValue.get().predictedValue

        List<RuleEvaluationResult> ruleEvaluations = []
        if (changePercent > algorithmRequest.upperPercentThreshold) {
            ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.BUY, 'Predicted percent change of ' + changePercent + '% is more than the upper threshold of ' + algorithmRequest.upperPercentThreshold + '%'))
        } else if (changePercent < algorithmRequest.lowerPercentThreshold) {
            ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.SELL, 'Predicted percent change of ' + changePercent + '% is less than the lower threshold of ' + algorithmRequest.lowerPercentThreshold + '%'))
        } else {
            ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.HOLD, 'Predicted percent change of ' + changePercent + '% is between lower the threshold of ' + algorithmRequest.lowerPercentThreshold + '% and upper threshold of ' + algorithmRequest.upperPercentThreshold))
        }

        if (predictedChange > algorithmRequest.upperPredictionPercentThreshold) {
            ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.BUY, 'Change in prediction of ' + predictedChange + ' is more than the upper threshold of ' + algorithmRequest.upperPredictionPercentThreshold))
        } else if (predictedChange < algorithmRequest.lowerPredictionPercentThreshold) {
            ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.SELL, 'Change in prediction of ' + predictedChange + ' is less than the lower threshold of ' + algorithmRequest.lowerPredictionPercentThreshold))
        } else {
            ruleEvaluations.push(new RuleEvaluationResult(RuleEvaluationAction.HOLD, 'Change in prediction of ' + predictedChange + ' is between lower the threshold of ' + algorithmRequest.lowerPredictionPercentThreshold + ' and upper threshold of ' + algorithmRequest.upperPredictionPercentThreshold))
        }
        return new PredictionRuleResult(ruleEvaluations*.message.join('\n'), (ruleEvaluations*.action.unique().size() == 1 ? ruleEvaluations*.action.unique().first() : RuleEvaluationAction.HOLD).name())
    }
}
