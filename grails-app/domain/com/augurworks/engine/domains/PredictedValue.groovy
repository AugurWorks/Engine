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

	Map getSlackMap(ActualValue actualValue = null) {
		String dateFormat = this.algorithmResult.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT
		String name = this.algorithmResult.algorithmRequest.dependantSymbol
		String aggregation = this.algorithmResult.algorithmRequest.dependentRequestDataSet.aggregation.name
		AlgorithmType modelType = this.algorithmResult.modelType
		TimeDuration runTime = use (TimeCategory) { new Date() - this.algorithmResult.dateCreated }
		String message = [
				'The prediction for ' + name + ' (' + aggregation + ') on ' + this.date.format(dateFormat) + ' from ' + modelType.name + ' is ' + this.value.round(4) + (actualValue != null ? ' with an un-aggregated value of ' + actualValue.getPredictedValue() : ''),
				'Run in ' + runTime.toString()
		].join('\n\n')
		return [
			message: message,
			channel: this.algorithmResult.algorithmRequest.slackChannel ?: Holders.config.augurworks.predictions.channel,
			color: this.value >= 0 ? '#4DBD33' : '#ff4444',
			title: this.algorithmResult.algorithmRequest.stringify(),
			link: Holders.config.grails.serverURL + '/algorithmRequest/show/' + this.algorithmResult.algorithmRequest.id
		]
	}

	String getSnsMessage(ActualValue actualValue = null) {
		Map slackMap = getSlackMap(actualValue)
		return slackMap.title + '\n\n' + slackMap.message
	}

	void sendToSlack(ActualValue actualValue = null, Optional<ActualValue> previousActualValue = Optional.empty()) {
		statsdClient.increment('count.slack.messages.sent')
		Map slackMap = this.getSlackMap(actualValue)
		new SlackMessage(slackMap.message, slackMap.channel).withBotName('Engine Predictions').withColor(slackMap.color).withTitle(slackMap.title).withLink(slackMap.link).send()
	}

	void sendToSns(ActualValue actualValue = null) {
		try {
			Product product = this.algorithmResult.algorithmRequest.product
			if (product) {
				AmazonSNSClient snsClient = new AmazonSNSClient()
				snsClient.publish(product.getSnsTopicArn(), getSnsMessage(actualValue))
			}
		} catch (Exception e) {
			log.error('Unable to send SNS message', e)
		}
	}
}
