package com.augurworks.engine.domains

import com.augurworks.engine.helper.TradingHours
import com.augurworks.engine.helper.Unit
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@Mock([AlgorithmRequest, AlgorithmResult, PredictedValue])
@Build([AlgorithmRequest, AlgorithmResult, PredictedValue])
@TestFor(AlgorithmResult)
class AlgorithmResultSpec extends Specification {

	void "test valid get tomorrows value (day)"() {
		given:
		AlgorithmResult result = AlgorithmResult.build()
		PredictedValue.build(algorithmResult: result, date: Unit.DAY.calculateOffset.apply(new Date(), 0))
		PredictedValue tomorrow = PredictedValue.build(algorithmResult: result, date: Unit.DAY.calculateOffset.apply(new Date(), 1))
		PredictedValue.build(algorithmResult: result, date: Unit.DAY.calculateOffset.apply(new Date(), 1))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == tomorrow
	}

	void "test valid get tomorrows value (hour)"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(unit: Unit.HOUR)
		AlgorithmResult result = AlgorithmResult.build(algorithmRequest: algorithmRequest, endDate: Unit.HOUR.calculateOffset.apply(new Date(), 0))
		PredictedValue nextHour = PredictedValue.build(algorithmResult: result, date: Unit.HOUR.calculateOffset.apply(new Date(), 1))
		PredictedValue.build(algorithmResult: result, date: Unit.HOUR.calculateOffset.apply(new Date(), 1))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == nextHour
	}

	void "test invalid get tomorrows value"() {
		given:
		AlgorithmResult result = AlgorithmResult.build(endDate: new Date())
		PredictedValue.build(algorithmResult: result, date: TradingHours.floorPeriod(new Date(), 24 * 60))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == null
	}

	void "test invalid get tomorrows value (hour)"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(unit: Unit.HOUR)
		AlgorithmResult result = AlgorithmResult.build(algorithmRequest: algorithmRequest, endDate: new Date())
		PredictedValue.build(algorithmResult: result, date: TradingHours.floorPeriod(new Date(), 60))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == null
	}
}
