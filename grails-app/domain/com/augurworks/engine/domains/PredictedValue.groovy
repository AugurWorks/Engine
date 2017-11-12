package com.augurworks.engine.domains

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

	Map getSlackMap(ActualValue actualValue = null) {
		String dateFormat = this.algorithmResult.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT
		String name = this.algorithmResult.algorithmRequest.dependantSymbol
		String aggregation = this.algorithmResult.algorithmRequest.dependentRequestDataSet.aggregation.name
		AlgorithmType modelType = this.algorithmResult.modelType
		TimeDuration runTime = use (TimeCategory) { new Date() - this.algorithmResult.dateCreated }
		Map<String, String> predictionAction = evaluatePredictionRules(actualValue)
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

	Map<String, String> evaluatePredictionRules(ActualValue actualValue) {
		if (!this.algorithmResult.algorithmRequest.upperPercentThreshold | !this.algorithmResult.algorithmRequest.lowerPercentThreshold) {
			return [
			        message: 'Prediction rules are not set'
			]
		}
		Double changePercent = (100 * (actualValue.getPredictedValue() - actualValue.getCurrentValue()) / actualValue.getCurrentValue()).round(1)
		if (changePercent > this.algorithmResult.algorithmRequest.upperPercentThreshold) {
			return [
			        action: 'BUY',
					message: 'Predicted percent change of ' + changePercent + '% is more than upper threshold of ' + this.algorithmResult.algorithmRequest.upperPercentThreshold + '%'
			]
		}
		if (changePercent < this.algorithmResult.algorithmRequest.lowerPercentThreshold) {
			return [
			        action: 'SELL',
					message: 'Predicted percent change of ' + changePercent + '% is less than lower threshold of ' + this.algorithmResult.algorithmRequest.lowerPercentThreshold + '%'
			]
		}
		return [
		        action: 'HOLD',
				message: 'Predicted percent change of ' + changePercent + '% is between lower threshold of ' + this.algorithmResult.algorithmRequest.lowerPercentThreshold + '% and upper threshold of ' + this.algorithmResult.algorithmRequest.upperPercentThreshold
		]
	}

	void sendToSlack(ActualValue actualValue = null) {
		statsdClient.increment('count.slack.messages.sent')
		Map slackMap = this.getSlackMap(actualValue)
		new SlackMessage(slackMap.message, slackMap.channel).withBotName('Engine Predictions').withColor(slackMap.color).withTitle(slackMap.title).withLink(slackMap.link).send()
	}
}
