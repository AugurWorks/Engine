package com.augurworks.engine.domains

import com.augurworks.engine.exceptions.AugurWorksException
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
        return volatility > product.volatilePercentLimit
    }

    Double getVolatility() {
        return 100 * realTimeResult.predictedDifference.abs() / (realTimeResult.actualValue - realTimeResult.predictedDifference).abs()
    }

    boolean isAllPositive() {
        return realTimePositive && closePositive
    }

    boolean isAllNegative() {
        return realTimeNegative && closeNegative
    }

    boolean isRealTimePositive() {
        return realTimeResult.predictedDifference > product.realTimeDiffThreshold && realTimeChange > product.realTimeChangeThreshold
    }

    boolean isRealTimeNegative() {
        return realTimeResult.predictedDifference < -product.realTimeDiffThreshold && realTimeChange < -product.realTimeChangeThreshold
    }

    boolean isClosePositive() {
        return closeResult.predictedDifference > product.closeDiffThreshold && closeChange > product.closeChangeThreshold
    }

    boolean isCloseNegative() {
        return closeResult.predictedDifference < -product.closeDiffThreshold && closeChange < -product.closeChangeThreshold
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
        try {
            if (!previousRun) {
                log.info('HOLD: There is no previous run')
                return RuleEvaluationAction.HOLD
            }
            if (!previousRun.previousRun) {
                log.info('HOLD: The previous run has no previous run')
                return RuleEvaluationAction.HOLD
            }
            if (tooVolatile) {
                log.info('HOLD: Current run is too volatile (Volatility: ' + volatility + ')')
                return RuleEvaluationAction.HOLD
            }
            if (previousRun.tooVolatile) {
                log.info('HOLD: Previous run is too volatile (Previous volatility: ' + previousRun.volatility + ')')
                return RuleEvaluationAction.HOLD
            }
            Double closeChange = closeChange
            Double previousCloseChange = previousRun.closeChange
            if (closeChange == null || previousCloseChange == null) {
                log.info('HOLD: Close change or previous close change is null, aborting. Close change: ' + closeChange + ', previous close change: ' + previousCloseChange)
                return RuleEvaluationAction.HOLD
            }
            if (closeChange > product.diffUpperThreshold && previousCloseChange > 0 && closeResult.predictedDifference > 0) {
                log.info('BUY: Close change upper matched, previous run is close change is greater than zero, close diff greater than zero (Close change: ' + closeChange.round(3) + ', Previous close change: ' + previousCloseChange.round(3) + ', Predicted difference: ' + closeResult.predictedDifference.round(3))
                return RuleEvaluationAction.BUY
            }
            if (closeChange < product.diffLowerThreshold && previousCloseChange < 0 && closeResult.predictedDifference < 0) {
                log.info('SELL: Close change lower matched, previous run close change is less than zero, close diff less than zero (Close change: ' + closeChange.round(3) + ', Previous close change: ' + previousCloseChange.round(3) + ', Predicted difference: ' + closeResult.predictedDifference.round(3))
                return RuleEvaluationAction.SELL
            }
            if (allPositive && previousRun.allNegative) {
                log.info('HOLD: Current run is all positive, previous run is all negative')
                return RuleEvaluationAction.HOLD
            }
            if (allNegative && previousRun.allPositive) {
                log.info('HOLD: Current run is all negative, previous run is all positive')
                return RuleEvaluationAction.HOLD
            }
            if (allPositive) {
                log.info('HOLD: Current run is all positive, previous run was not all negative')
                return RuleEvaluationAction.BUY
            }
            if (allNegative) {
                log.info('HOLD: Current run is all negative, previous run was not all positive')
                return RuleEvaluationAction.SELL
            }
            log.info('HOLD: No rules matched')
            return RuleEvaluationAction.HOLD
        } catch (Exception e) {
            log.error('HOLD: An exception occurred', e)
            return RuleEvaluationAction.HOLD
        }
    }

    String getSlackChannel() {
        List<String> channels = [realTimeResult, closeResult]*.algorithmRequest*.slackChannel.unique()
        return channels.size() == 1 ? channels.get(0) : null
    }

    private Double getDiff(Double currentValue, Double previousValue) {
        if (currentValue == null || previousValue == null) {
            throw new AugurWorksException("Current value or previous value does not exist for this Product Result: " + this.id + ", Current Value: " + currentValue + ", Previous Value: " + previousValue)
        }
        return currentValue - previousValue
    }
}
