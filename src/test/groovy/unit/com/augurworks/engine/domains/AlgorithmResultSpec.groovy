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
		AlgorithmResult result = AlgorithmResult.build(dateCreated: new Date())
		PredictedValue.build(algorithmResult: result, date: Unit.DAY.calculateOffset.apply(new Date(), 0))
		PredictedValue tomorrow = PredictedValue.build(algorithmResult: result, date: Unit.DAY.calculateOffset.apply(new Date(), 1))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == tomorrow
	}

	void "test valid get tomorrows value (hour)"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(unit: Unit.HOUR, dateCreated: new Date())
		AlgorithmResult result = AlgorithmResult.build(algorithmRequest: algorithmRequest, endDate: Unit.HOUR.calculateOffset.apply(new Date(), 0), dateCreated: new Date())
		PredictedValue nextHour = PredictedValue.build(algorithmResult: result, date: Unit.HOUR.calculateOffset.apply(new Date(), 1))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == nextHour
	}

	void "test invalid get tomorrows value (hour)"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(unit: Unit.HOUR, dateCreated: new Date())
		AlgorithmResult result = AlgorithmResult.build(algorithmRequest: algorithmRequest, endDate: new Date(), dateCreated: new Date())
		PredictedValue.build(algorithmResult: result, date: TradingHours.floorPeriod(new Date(), 60))

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == null
	}
}
