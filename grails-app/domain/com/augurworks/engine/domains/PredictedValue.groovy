package com.augurworks.engine.domains

import com.amazonaws.services.sns.AmazonSNSClient
import com.augurworks.engine.data.ActualValue
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Global
import com.augurworks.engine.instrumentation.Instrumentation
import com.augurworks.engine.slack.SlackMessage
import com.timgroup.statsd.StatsDClient
import grails.util.Holders
import groovy.time.TimeCategory
import groovy.time.TimeDuration

class PredictedValue {

	private final StatsDClient statsdClient = Instrumentation.statsdClient

	Date date
	double value
	Double actual

	static belongsTo = [algorithmResult: AlgorithmResult]

	static constraints = {
		actual nullable: true
	}

	String toString() {
		return date.format(Global.DATE_FORMAT) + ': ' + value.round(4)
	}

	Map toMap() {
		return [
			date: date.format(Global.C3_DATE_FORMAT),
			value: value
		]
	}

	Map getSlackMap(ActualValue actualValue = null, Optional<ActualValue> previousActualValue = Optional.empty()) {
		String dateFormat = this.algorithmResult.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT
		String name = this.algorithmResult.algorithmRequest.dependantSymbol
		String aggregation = this.algorithmResult.algorithmRequest.dependentRequestDataSet.aggregation.name
		AlgorithmType modelType = this.algorithmResult.modelType
		TimeDuration runTime = use (TimeCategory) { new Date() - this.algorithmResult.dateCreated }
		Map<String, String> predictionAction = evaluatePredictionRules(actualValue, previousActualValue)
		String message = [
				predictionAction.message,
				'The prediction for ' + name + ' (' + aggregation + ') on ' + this.date.format(dateFormat) + ' from ' + modelType.name + ' is ' + this.value.round(4) + (actualValue != null ? ' with an un-aggregated value of ' + actualValue.getPredictedValue() : ''),
				'Run in ' + runTime.toString()
		].join('\n\n')
		return [
			message: message,
			channel: this.algorithmResult.algorithmRequest.slackChannel ?: Holders.config.augurworks.predictions.channel,
			color: this.value >= 0 ? '#4DBD33' : '#ff4444',
			title: (predictionAction.action ? predictionAction.action + ' - ' : '') + this.algorithmResult.algorithmRequest.stringify(),
			link: Holders.config.grails.serverURL + '/algorithmRequest/show/' + this.algorithmResult.algorithmRequest.id
		]
	}

	String getSnsMessage(ActualValue actualValue = null, Optional<ActualValue> previousActualValue = Optional.empty()) {
		Map slackMap = getSlackMap(actualValue, previousActualValue)
		return slackMap.title + '\n\n' + slackMap.message
	}

	Map<String, String> evaluatePredictionRules(ActualValue actualValue, Optional<ActualValue> previousActualValue = Optional.empty()) {
		if (actualValue == null || !previousActualValue.isPresent()) {
			return [
					message: 'Current or previous run data is missing'
			]
		}
		AlgorithmResult algorithmResult = this.algorithmResult
		AlgorithmRequest algorithmRequest = algorithmResult.algorithmRequest
		if (algorithmRequest.upperPercentThreshold == null || algorithmRequest.lowerPercentThreshold == null || algorithmRequest.upperPredictionPercentThreshold == null || algorithmRequest.lowerPredictionPercentThreshold == null) {
			return [
			        message: 'All prediction rules must be set'
			]
		}
		Double changePercent = (100 * (actualValue.getPredictedValue() - actualValue.getCurrentValue()) / actualValue.getCurrentValue()).round(3)
		Double predictedChange = actualValue.predictedValue - previousActualValue.get().predictedValue

		List<RuleEvaluation> ruleEvaluations = []
		if (changePercent > algorithmRequest.upperPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluation(Action.BUY, 'Predicted percent change of ' + changePercent + '% is more than the upper threshold of ' + algorithmRequest.upperPercentThreshold + '%'))
		} else if (changePercent < algorithmRequest.lowerPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluation(Action.SELL, 'Predicted percent change of ' + changePercent + '% is less than the lower threshold of ' + algorithmRequest.lowerPercentThreshold + '%'))
		} else {
			ruleEvaluations.push(new RuleEvaluation(Action.HOLD, 'Predicted percent change of ' + changePercent + '% is between lower the threshold of ' + algorithmRequest.lowerPercentThreshold + '% and upper threshold of ' + algorithmRequest.upperPercentThreshold))
		}

		if (predictedChange > algorithmRequest.upperPredictionPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluation(Action.BUY, 'Change in prediction of ' + predictedChange + ' is more than the upper threshold of ' + algorithmRequest.upperPredictionPercentThreshold))
		} else if (predictedChange < algorithmRequest.lowerPredictionPercentThreshold) {
			ruleEvaluations.push(new RuleEvaluation(Action.SELL, 'Change in prediction of ' + predictedChange + ' is less than the lower threshold of ' + algorithmRequest.lowerPredictionPercentThreshold))
		} else {
			ruleEvaluations.push(new RuleEvaluation(Action.HOLD, 'Change in prediction of ' + predictedChange + ' is between lower the threshold of ' + algorithmRequest.lowerPredictionPercentThreshold + ' and upper threshold of ' + algorithmRequest.upperPredictionPercentThreshold))
		}

		return [
			action: (ruleEvaluations*.action.unique().size() == 1 ? ruleEvaluations*.action.unique().first() : Action.HOLD).name(),
			message: ruleEvaluations*.message.join('\n')
		]
	}

	void sendToSlack(ActualValue actualValue = null, Optional<ActualValue> previousActualValue = Optional.empty()) {
		statsdClient.increment('count.slack.messages.sent')
		Map slackMap = this.getSlackMap(actualValue, previousActualValue)
		new SlackMessage(slackMap.message, slackMap.channel).withBotName('Engine Predictions').withColor(slackMap.color).withTitle(slackMap.title).withLink(slackMap.link).send()
	}

	void sendToSns(ActualValue actualValue = null, Optional<ActualValue> previousActualValue = Optional.empty()) {
		try {
			Product product = this.algorithmResult.algorithmRequest.product
			if (product) {
				AmazonSNSClient snsClient = new AmazonSNSClient()
				snsClient.publish(product.getSnsTopicArn(), getSnsMessage(actualValue, previousActualValue))
			}
		} catch (Exception e) {
			log.error('Unable to send SNS message', e)
		}
	}

	private class RuleEvaluation {

		RuleEvaluation(Action action, String message) {
			this.action = action
			this.message = message
		}

		private final Action action
		private final String message
	}

	private enum Action {
		BUY,
		SELL,
		HOLD
	}
}
