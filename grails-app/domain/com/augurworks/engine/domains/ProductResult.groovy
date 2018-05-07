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
    transient Boolean realTimePositive
    transient Boolean realTimeNegative
    transient Boolean closePositive
    transient Boolean closeNegative
    transient Double realTimeChange
    transient Double closeChange
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
        return realTimePositive && closePositive
    }

    boolean isAllNegative() {
        return realTimeNegative && closeNegative
    }

    boolean isRealTimePositive() {
        return 100 * realTimeChange / previousRun.realTimeResult.actualValue > product.isRealTimePositiveThresholdPercent
    }

    boolean isRealTimeNegative() {
        return 100 * realTimeChange / previousRun.realTimeResult.actualValue < product.isRealTimeNegativeThresholdPercent
    }

    boolean isClosePositive() {
        return 100 * closeChange / previousRun.closeResult.actualValue > product.isClosePositiveThresholdPercent
    }

    boolean isCloseNegative() {
        return 100 * closeChange / previousRun.closeResult.actualValue < product.isCloseNegativeThresholdPercent
    }

    // aka RT Change
    Double getRealTimeChange() {
        return getDiff(realTimeResult.actualValue, previousRun?.realTimeResult?.actualValue)
    }

    // aka Close Change
    Double getCloseChange() {
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
        if (!previousRun.previousRun) {
            log.debug('HOLD: The previous run has no previous run')
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
        if (closeChange > product.diffUpperThreshold && previousRun.closePositive && closeResult.predictedDifference > 0) {
            log.debug('BUY: Close diff upper matched, previous run is positive')
            return RuleEvaluationAction.BUY
        }
        if (closeChange < product.diffLowerThreshold && previousRun.closeNegative && closeResult.predictedDifference < 0) {
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
        if (allPositive) {
            log.debug('HOLD: Current run is all positive, previous run was not all negative')
            return RuleEvaluationAction.BUY
        }
        if (allNegative) {
            log.debug('HOLD: Current run is all negative, previous run was not all positive')
            return RuleEvaluationAction.SELL
        }
        log.debug('HOLD: No rules matched')
        return RuleEvaluationAction.HOLD
    }

    String getSlackChannel() {
        List<String> channels = [realTimeResult, closeResult]*.algorithmRequest*.slackChannel.unique()
        return channels.size() == 1 ? channels.get(0) : null
    }

    private Double getDiff(Double currentValue, Double previousValue) {
        if (!currentValue || !previousValue) {
            return null
        }
        return currentValue - previousValue
    }
}
