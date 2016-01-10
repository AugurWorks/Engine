package com.augurworks.engine.domains

import grails.util.Holders

import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.SlackMessage

class PredictedValue {

	Date date
	double value

	static belongsTo = [algorithmResult: AlgorithmResult]

	static constraints = {
		date()
		value()
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

	void sendToSlack() {
		String dateFormat = this.algorithmResult.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT
		String name = this.algorithmResult.algorithmRequest.dependantDataSet.name
		String aggregation = this.algorithmResult.algorithmRequest.dependentRequestDataSet.aggregation
		String message = 'The prediction for ' + name + ' (' + aggregation + ') on ' + this.date.format(dateFormat) + ' is ' + this.value.round(4)
		String channel = Holders.config.augurworks.predictions.channel
		String title = this.algorithmResult.algorithmRequest.stringify()
		String link = Holders.config.grails.serverURL + '/algorithmRequest/show/' + this.algorithmResult.algorithmRequest.id
		String color = this.value >=0 ? '#4DBD33' : '#ff4444'
		new SlackMessage(message, channel).withBotName('Engine Predictions').withColor(color).withTitle(title).withLink(link).send()
	}
}
