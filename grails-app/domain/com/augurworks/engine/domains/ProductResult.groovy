package com.augurworks.engine.domains

import com.augurworks.engine.model.prediction.RuleEvaluationAction

class ProductResult {

    Date adjustedDateCreated
    ProductResult previousRun
    AlgorithmResult realTimeResult
    AlgorithmResult closeResult

    static belongsTo = [product: Product]

    Product product

    static constraints = {
        previousRun nullable: true
        realTimeResult nullable: true
        closeResult nullable: true
    }

    boolean isTooVolatile() {
        return 100 * realTimeDiff.abs() / previousRun.realTimeResult.actualValue.abs() > product.volatilePercentLimit
    }

    boolean isAllPositive() {
        return [realTimeDiff, closeDiff].collect { it > 0 }.every()
    }

    boolean isAllNegative() {
        return [realTimeDiff, closeDiff].collect { it < 0 }.every()
    }

    Double getRealTimeDiff() {
        return getDiff(realTimeResult.actualValue, previousRun?.realTimeResult?.actualValue)
    }

    Double getCloseDiff() {
        return getDiff(closeResult.actualValue, previousRun?.closeResult?.actualValue)
    }

    RuleEvaluationAction getAction() {
        if (!previousRun) {
            return RuleEvaluationAction.HOLD
        }
        if (realTimeDiff > product.realTimeDiffUpper && previousRun.realTimeDiff > 0) {
            return RuleEvaluationAction.BUY
        }
        if (realTimeDiff < product.realTimeDiffLower && previousRun.realTimeDiff < 0) {
            return RuleEvaluationAction.SELL
        }
        if (tooVolatile || previousRun.tooVolatile) {
            return RuleEvaluationAction.HOLD
        }
        if ((allPositive && previousRun.allNegative) || (allNegative && previousRun.allPositive)) {
            return RuleEvaluationAction.HOLD
        }
        List<RuleEvaluationAction> actions = [realTimeResult, closeResult]*.evaluateRules()*.action.unique()
        if (actions.size() == 1) {
            return actions.get(0)
        }
        return RuleEvaluationAction.HOLD
    }

    String getSlackChannel() {
        List<String> channels = [realTimeResult, closeResult]*.algorithmRequest*.slackChannel.unique()
        return channels.size() == 0 ? channels.get(0) : null
    }

    private Double getDiff(Double currentValue, Double previousValue) {
        if (!currentValue || !previousValue) {
            return null
        }
        return currentValue - previousValue
    }
}
