package com.augurworks.engine.domains

import com.augurworks.engine.helper.TradingHours
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.prediction.PredictionRuleResult
import com.augurworks.engine.model.prediction.RuleEvaluationAction
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

		when:
		PredictedValue actual = result.futureValue

		then:
		actual == nextHour
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

	void "test evaluate hold rules"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(
				upperPercentThreshold: 1,
				lowerPercentThreshold: -1,
				upperPredictionPercentThreshold: 1,
				lowerPredictionPercentThreshold: -1
		)
		AlgorithmResult firstResult = AlgorithmResult.build(
				algorithmRequest: algorithmRequest,
				actualValue: 10
		)
		AlgorithmResult secondResult = AlgorithmResult.build(
				algorithmRequest: algorithmRequest,
				actualValue: 20,
				previousAlgorithmResult: firstResult
		)

		when:
		PredictionRuleResult ruleResult = secondResult.evaluateRules()

		then:
		ruleResult.action == RuleEvaluationAction.HOLD
	}

	void "test evaluate buy rules"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(
				upperPercentThreshold: 1,
				lowerPercentThreshold: -1,
				upperPredictionPercentThreshold: 1,
				lowerPredictionPercentThreshold: -1
		)
		AlgorithmResult firstResult = AlgorithmResult.build(
				algorithmRequest: algorithmRequest,
				actualValue: 10,
				predictedDifference: 2
		)
		AlgorithmResult secondResult = AlgorithmResult.build(
				algorithmRequest: algorithmRequest,
				actualValue: 15,
				predictedDifference: 2,
				previousAlgorithmResult: firstResult
		)
		AlgorithmResult thirdResult = AlgorithmResult.build(
				algorithmRequest: algorithmRequest,
				actualValue: 20,
				predictedDifference: 2,
				previousAlgorithmResult: secondResult
		)

		when:
		PredictionRuleResult ruleResult = thirdResult.evaluateRules()

		then:
		ruleResult.action == RuleEvaluationAction.BUY
	}
}
