package com.augurworks.engine.domains

import com.augurworks.engine.instrumentation.Instrumentation
import com.timgroup.statsd.StatsDClient
import grails.util.Holders
import groovy.time.TimeCategory
import groovy.time.TimeDuration

import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Global
import com.augurworks.engine.slack.SlackMessage

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

	Map getSlackMap(Double actual = null) {
		String dateFormat = this.algorithmResult.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT
		String name = this.algorithmResult.algorithmRequest.dependantSymbol
		String aggregation = this.algorithmResult.algorithmRequest.dependentRequestDataSet.aggregation.name
		AlgorithmType modelType = this.algorithmResult.modelType
		TimeDuration runTime = use (TimeCategory) { new Date() - this.algorithmResult.dateCreated }
		return [
			message: 'The prediction for ' + name + ' (' + aggregation + ') on ' + this.date.format(dateFormat) + ' from ' + modelType.name + ' is ' + this.value.round(4) + (actual != null ? ' with an un-aggregated value of ' + actual : '') + '\nRun in ' + runTime.toString(),
			channel: this.algorithmResult.algorithmRequest.slackChannel ?: Holders.config.augurworks.predictions.channel,
			color: this.value >= 0 ? '#4DBD33' : '#ff4444',
			title: this.algorithmResult.algorithmRequest.stringify(),
			link: Holders.config.grails.serverURL + '/algorithmRequest/show/' + this.algorithmResult.algorithmRequest.id
		]
	}

	void sendToSlack(Double actual = null) {
		statsdClient.increment('count.slack.messages.sent')
		Map slackMap = this.getSlackMap(actual)
		new SlackMessage(slackMap.message, slackMap.channel).withBotName('Engine Predictions').withColor(slackMap.color).withTitle(slackMap.title).withLink(slackMap.link).send()
	}
}
