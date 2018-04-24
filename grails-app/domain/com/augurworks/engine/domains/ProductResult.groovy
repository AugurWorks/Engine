package com.augurworks.engine.domains

import com.augurworks.engine.model.prediction.RuleEvaluationAction
import org.slf4j.MDC

class ProductResult {

    Date adjustedDateCreated
    ProductResult previousRun
    AlgorithmResult realTimeResult
    AlgorithmResult closeResult

    static belongsTo = [product: Product]

    transient Boolean tooVolatile
    transient Boolean allPositive
    transient Boolean allNegative
    transient Double realTimeDiff
    transient Double closeDiff
    transient RuleEvaluationAction action
    transient String slackChannel

    static constraints = {
        previousRun nullable: true
        realTimeResult nullable: true
        closeResult nullable: true
    }

    boolean isTooVolatile() {
        return 100 * realTimeDiff.abs() / previousRun.realTimeResult.actualValue.abs() > product.volatilePercentLimit
    }

    boolean isAllPositive() {
        return 100 * realTimeDiff / previousRun.realTimeResult.actualValue > product.isRealTimePositiveThresholdPercent && 100 * closeDiff / previousRun.closeResult.actualValue > product.isClosePositiveThresholdPercent
    }

    boolean isAllNegative() {
        return 100 * realTimeDiff / previousRun.realTimeResult.actualValue < product.isRealTimeNegativeThresholdPercent && 100 * closeDiff / previousRun.closeResult.actualValue < product.isCloseNegativeThresholdPercent
    }

    Double getRealTimeDiff() {
        return getDiff(realTimeResult.actualValue, previousRun?.realTimeResult?.actualValue)
    }

    Double getCloseDiff() {
        return getDiff(closeResult.actualValue, previousRun?.closeResult?.actualValue)
    }

    RuleEvaluationAction getAction() {
        MDC.put('product', product.id.toString())
        MDC.put('productName', product.name)
        MDC.put('productResult', id.toString())
        if (!previousRun) {
            log.debug('HOLD: There is no previous run')
            return RuleEvaluationAction.HOLD
        }
        if (tooVolatile) {
            log.debug('HOLD: Current run is too volatile')
            return RuleEvaluationAction.HOLD
        }
        if (previousRun.tooVolatile) {
            log.debug('HOLD: Previous run is too volatile')
            return RuleEvaluationAction.HOLD
        }
        if (closeDiff > product.diffUpperThreshold && previousRun.closeDiff > 0 && closeResult.predictedDifference > 0) {
            log.debug('BUY: Close diff upper matched, previous run is positive')
            return RuleEvaluationAction.BUY
        }
        if (closeDiff < product.diffLowerThreshold && previousRun.closeDiff < 0 && closeResult.predictedDifference < 0) {
            log.debug('SELL: Close diff lower matched, previous run is negative')
            return RuleEvaluationAction.SELL
        }
        if (allPositive && previousRun.allNegative) {
            log.debug('HOLD: Current run is all positive, previous run is all negative')
            return RuleEvaluationAction.HOLD
        }
        if (allNegative && previousRun.allPositive) {
            log.debug('HOLD: Current run is all negative, previous run is all positive')
            return RuleEvaluationAction.HOLD
        }
        List<RuleEvaluationAction> actions = [realTimeResult, closeResult]*.evaluateRules()*.action.unique()
        if (actions.size() == 1) {
            log.debug(actions.get(0) + ': Real time and close actions match')
            return actions.get(0)
        }
        log.debug('HOLD: Real time and close actions did not match')
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
