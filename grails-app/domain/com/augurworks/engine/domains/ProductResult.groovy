package com.augurworks.engine.domains

import com.amazonaws.services.sns.AmazonSNSClient
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.model.prediction.RuleEvaluationAction
import com.augurworks.engine.model.prediction.RuleEvaluationReason
import grails.util.Pair
import org.slf4j.MDC

import java.text.DateFormat
import java.text.SimpleDateFormat

class ProductResult {

    private static DateFormat getTimeFormat() {
        return new SimpleDateFormat('HH:mm')
    }

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
        return isRealTimePositive() && isClosePositive()
    }

    boolean isAllNegative() {
        return isRealTimeNegative() && isCloseNegative()
    }

    boolean isRealTimePositive() {
        return realTimeResult.predictedDifference > product.realTimeDiffThreshold && getRealTimeChange() > product.realTimeChangeThreshold
    }

    boolean isRealTimeNegative() {
        return realTimeResult.predictedDifference < -product.realTimeDiffThreshold && getRealTimeChange() < -product.realTimeChangeThreshold
    }

    boolean isClosePositive() {
        return closeResult.predictedDifference > product.closeDiffThreshold && getCloseChange() > product.closeChangeThreshold
    }

    boolean isCloseNegative() {
        return closeResult.predictedDifference < -product.closeDiffThreshold && getCloseChange() < -product.closeChangeThreshold
    }

    // aka RT Change
    Double getRealTimeChange() {
        return getDiff(realTimeResult.actualValue, previousRun?.realTimeResult?.actualValue)
    }

    // aka Close Change
    Double getCloseChange() {
        return getDiff(closeResult.actualValue, previousRun?.closeResult?.actualValue)
    }

    Pair<RuleEvaluationAction, RuleEvaluationReason> getAction() {
        MDC.put('product', product.id.toString())
        MDC.put('productName', product.name)
        MDC.put('productResult', id.toString())
        try {
            if (!previousRun) {
                log.info('HOLD: There is no previous run')
                return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.MISSING_PREVIOUS_RUN)
            }
            if (!previousRun.previousRun) {
                log.info('HOLD: The previous run has no previous run')
                return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.MISSING_DATA)
            }
            if (isTooVolatile()) {
                log.info('HOLD: Current run is too volatile (Volatility: ' + volatility + ')')
                return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.TOO_VOLATILE)
            }
            if (previousRun.isTooVolatile()) {
                log.info('HOLD: Previous run is too volatile (Previous volatility: ' + previousRun.volatility + ')')
                return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.TOO_VOLATILE)
            }
            Double currentCloseChange = getCloseChange()
            Double previousCloseChange = previousRun.getCloseChange()
            if (currentCloseChange == null || previousCloseChange == null) {
                log.info('HOLD: Close change or previous close change is null, aborting. Close change: ' + currentCloseChange + ', previous close change: ' + previousCloseChange)
                return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.MISSING_DATA)
            }
            if (currentCloseChange < product.diffUpperThreshold && currentCloseChange > product.diffLowerThreshold && previousCloseChange > 0 && closeResult.predictedDifference > 0) {
                log.info('BUY: Close change upper matched, previous run is close change is greater than zero, close diff greater than zero (Close change: ' + currentCloseChange.round(3) + ', Previous close change: ' + previousCloseChange.round(3) + ', Predicted difference: ' + closeResult.predictedDifference.round(3))
                return new Pair<>(RuleEvaluationAction.BUY, RuleEvaluationReason.CLOSE_CHANGE_THRESHOLD)
            }
            if (currentCloseChange > -product.diffUpperThreshold && currentCloseChange < -product.diffLowerThreshold && previousCloseChange < 0 && closeResult.predictedDifference < 0) {
                log.info('SELL: Close change lower matched, previous run close change is less than zero, close diff less than zero (Close change: ' + currentCloseChange.round(3) + ', Previous close change: ' + previousCloseChange.round(3) + ', Predicted difference: ' + closeResult.predictedDifference.round(3))
                return new Pair<>(RuleEvaluationAction.SELL, RuleEvaluationReason.CLOSE_CHANGE_THRESHOLD)
            }
            if (isAllPositive() && previousRun.isAllNegative() && previousRun.getAction().getbValue() != RuleEvaluationReason.PREVIOUS_RUN_MATCHED) {
                log.info('HOLD: Current run is all positive, previous run is all negative')
                return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.PREVIOUS_RUN_MATCHED)
            }
            if (isAllNegative() && previousRun.isAllPositive() && previousRun.getAction().getbValue() != RuleEvaluationReason.PREVIOUS_RUN_MATCHED) {
                log.info('HOLD: Current run is all negative, previous run is all positive')
                return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.PREVIOUS_RUN_MATCHED)
            }
            if (isAllPositive()) {
                log.info('BUY: Current run is all positive, previous run was not all negative')
                return new Pair<>(RuleEvaluationAction.BUY, RuleEvaluationReason.ALL_SAME_DIRECTION)
            }
            if (isAllNegative()) {
                log.info('SELL: Current run is all negative, previous run was not all positive')
                return new Pair<>(RuleEvaluationAction.SELL, RuleEvaluationReason.ALL_SAME_DIRECTION)
            }
            log.info('HOLD: No rules matched')
            return new Pair<>(RuleEvaluationAction.HOLD, RuleEvaluationReason.NO_RULES_MATCHED)
        } catch (Exception e) {
            log.error('HOLD: An exception occurred', e)
            throw e
        }
    }

    String getSlackChannel() {
        List<String> channels = [realTimeResult, closeResult]*.algorithmRequest*.slackChannel.unique()
        return channels.size() == 1 ? channels.get(0) : null
    }

    void sendToSns() {
        try {
            new AmazonSNSClient().publish(product.getSnsTopicArn(), getSnsMessage())
        } catch (Exception e) {
            log.error('Unable to send SNS message', e)
        }
    }

    String getSnsMessage() {
        String message = getTimeFormat().format(new Date()) + ' - ' + product.name + ' - '
        try {
            return message + getAction().getaValue().name()
        } catch (Exception e) {
            return message + RuleEvaluationAction.HOLD.name() + ' - An exception occurred, defaulting to HOLD'
        }
    }

    private Double getDiff(Double currentValue, Double previousValue) {
        if (currentValue == null || previousValue == null) {
            throw new AugurWorksException("Current value or previous value does not exist for this Product Result: " + this.id + ", Current Value: " + currentValue + ", Previous Value: " + previousValue)
        }
        return currentValue - previousValue
    }
}
