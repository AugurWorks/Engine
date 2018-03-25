package com.augurworks.engine.model.prediction

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult

class PredictionRuleResult {

    public String message
    public RuleEvaluationAction action
    public String actionMessage

    private PredictionRuleResult(String message) {
        this(message, null)
    }

    private PredictionRuleResult(String message, RuleEvaluationAction action) {
        this.message = message
        this.action = action
    }

    static PredictionRuleResult create(AlgorithmResult algorithmResult) {
        AlgorithmRequest algorithmRequest = algorithmResult.algorithmRequest
        if (!algorithmResult.actualValue || !algorithmResult?.previousAlgorithmResult?.actualValue) {
            return new PredictionRuleResult('Current or previous run data is missing')
        }
        if (!algorithmRequest.upperPercentThreshold || !algorithmRequest.lowerPercentThreshold || !algorithmRequest.upperPredictionPercentThreshold || !algorithmRequest.lowerPredictionPercentThreshold) {
            return new PredictionRuleResult('All prediction rules must be set')
        }
        Double predictedDifference = algorithmResult.predictedDifference
        Double predictedChange = algorithmResult.predictionChange

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
        return new PredictionRuleResult(ruleEvaluations*.message.join('\n'), (ruleEvaluations*.action.unique().size() == 1 ? ruleEvaluations*.action.unique().first() : RuleEvaluationAction.HOLD).name())
    }
}
