package com.augurworks.engine.domains

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import groovy.time.TimeCategory

import org.apache.commons.lang.time.DateUtils

import spock.lang.Specification

@Mock([AlgorithmResult, PredictedValue])
@Build([AlgorithmResult, PredictedValue])
@TestFor(AlgorithmResult)
class AlgorithmResultSpec extends Specification {

	void "test valid get tomorrows value"() {
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

	void "test invalid get tomorrows value"() {
		given:
		AlgorithmResult result = AlgorithmResult.build()
		PredictedValue.build(algorithmResult: result, date: DateUtils.truncate(new Date(), Calendar.DATE))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == null
	}
}
