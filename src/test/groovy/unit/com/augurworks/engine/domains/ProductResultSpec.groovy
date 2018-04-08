package unit.com.augurworks.engine.domains

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.Product
import com.augurworks.engine.domains.ProductResult
import com.augurworks.engine.model.prediction.RuleEvaluationAction
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@Mock([AlgorithmResult,ProductResult, Product])
@Build([AlgorithmResult, ProductResult, Product])
@TestFor(ProductResult)
class ProductResultSpec extends Specification {

	void "test is all positive"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult previousCloseResult = AlgorithmResult.build(actualValue: 1000)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 1000)
		ProductResult previousResult = ProductResult.build(realTimeResult: previousRealTimeResult, closeResult: previousCloseResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 1001)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 1001)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		Boolean isAllPositive = productResult.allPositive

		then:
		isAllPositive
	}

	void "test is not all positive"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult previousCloseResult = AlgorithmResult.build(actualValue: 1000)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 1000)
		ProductResult previousResult = ProductResult.build(realTimeResult: previousRealTimeResult, closeResult: previousCloseResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 1001)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 999)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		Boolean isAllPositive = productResult.allPositive

		then:
		!isAllPositive
	}

	void "test null previous run"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult realTimeResult = AlgorithmResult.build()
		AlgorithmResult closeResult = AlgorithmResult.build()
		ProductResult productResult = ProductResult.build(realTimeResult: realTimeResult, closeResult: closeResult, product: product)
		productResult.previousRun = null

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.HOLD
	}

	void "test both up"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult closeResult = AlgorithmResult.build()
		AlgorithmResult twoPreviousRealTimeResult = AlgorithmResult.build(actualValue: 100)
		ProductResult twoPreviousResult = ProductResult.build(realTimeResult: twoPreviousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 101)
		ProductResult previousResult = ProductResult.build(previousRun: twoPreviousResult, realTimeResult: previousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 115)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.BUY
	}

	void "test both down"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult closeResult = AlgorithmResult.build()
		AlgorithmResult twoPreviousRealTimeResult = AlgorithmResult.build(actualValue: 100)
		ProductResult twoPreviousResult = ProductResult.build(realTimeResult: twoPreviousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 99)
		ProductResult previousResult = ProductResult.build(previousRun: twoPreviousResult, realTimeResult: previousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 85)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.SELL
	}

	void "test too volatile"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult closeResult = AlgorithmResult.build()
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 1)
		ProductResult previousResult = ProductResult.build(realTimeResult: previousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: -15)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.HOLD
	}

	void "test previous too volatile"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult closeResult = AlgorithmResult.build()
		AlgorithmResult twoPreviousRealTimeResult = AlgorithmResult.build(actualValue: 1)
		ProductResult twoPreviousResult = ProductResult.build(realTimeResult: twoPreviousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: -15)
		ProductResult previousResult = ProductResult.build(previousRun: twoPreviousResult, realTimeResult: previousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: -15)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.HOLD
	}

	void "test all opposite"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, realTimeDiffUpper: 11, realTimeDiffLower: -11)
		AlgorithmResult twoPreviousCloseResult = AlgorithmResult.build(actualValue: 1000)
		AlgorithmResult twoPreviousRealTimeResult = AlgorithmResult.build(actualValue: 1000)
		ProductResult twoPreviousResult = ProductResult.build(realTimeResult: twoPreviousRealTimeResult, closeResult: twoPreviousCloseResult, product: product)
		AlgorithmResult previousCloseResult = AlgorithmResult.build(actualValue: 1001)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 1001)
		ProductResult previousResult = ProductResult.build(previousRun: twoPreviousResult, realTimeResult: previousRealTimeResult, closeResult: previousCloseResult, product: product)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 1000)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 1000)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.HOLD
	}
}
