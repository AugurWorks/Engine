package com.augurworks.engine.domains

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import groovy.time.TimeCategory

import org.apache.commons.lang.time.DateUtils

import spock.lang.Specification

@Mock([AlgorithmRequest, AlgorithmResult, PredictedValue])
@Build([AlgorithmRequest, AlgorithmResult, PredictedValue])
@TestFor(AlgorithmResult)
class AlgorithmResultSpec extends Specification {

	void "test valid get tomorrows value (day)"() {
		given:
		AlgorithmResult result = AlgorithmResult.build()
		PredictedValue.build(algorithmResult: result, date: DateUtils.truncate(new Date(), Calendar.DATE))
		PredictedValue tomorrow = PredictedValue.build(algorithmResult: result, date: DateUtils.ceiling(new Date(), Calendar.DATE))
		PredictedValue.build(algorithmResult: result, date: use(TimeCategory) { DateUtils.ceiling(new Date(), Calendar.DATE) + 1.days })

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == tomorrow
	}

	void "test valid get tomorrows value (hour)"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(unit: 'Hour')
		AlgorithmResult result = AlgorithmResult.build(algorithmRequest: algorithmRequest, endDate: new Date())
		PredictedValue nextHour = PredictedValue.build(algorithmResult: result, date: DateUtils.ceiling(new Date(), Calendar.HOUR))
		PredictedValue.build(algorithmResult: result, date: DateUtils.truncate(new Date(), Calendar.HOUR))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == nextHour
	}

	void "test invalid get tomorrows value"() {
		given:
		AlgorithmResult result = AlgorithmResult.build(endDate: new Date())
		PredictedValue.build(algorithmResult: result, date: DateUtils.truncate(new Date(), Calendar.DATE))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == null
	}

	void "test invalid get tomorrows value (hour)"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(unit: 'Hour')
		AlgorithmResult result = AlgorithmResult.build(algorithmRequest: algorithmRequest, endDate: new Date())
		PredictedValue.build(algorithmResult: result, date: DateUtils.truncate(new Date(), Calendar.HOUR))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == null
	}
}
