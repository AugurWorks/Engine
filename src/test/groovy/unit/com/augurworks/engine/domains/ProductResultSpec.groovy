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
		Product product = Product.build(volatilePercentLimit: 0.4, diffUpperThreshold: 11, diffLowerThreshold: -11, realTimeDiffThreshold: 0.1, closeDiffThreshold: 0.1)
		AlgorithmResult previousCloseResult = AlgorithmResult.build(actualValue: 1000, predictedDifference: 5)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 1000, predictedDifference: 5)
		ProductResult previousResult = ProductResult.build(realTimeResult: previousRealTimeResult, closeResult: previousCloseResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 1002, predictedDifference: 5)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 1002, predictedDifference: 5)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		Boolean isAllPositive = productResult.isAllPositive()

		then:
		isAllPositive
	}

	void "test is not all positive"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, diffUpperThreshold: 11, diffLowerThreshold: -11, realTimeDiffThreshold: 0.1, closeDiffThreshold: 0.1)
		AlgorithmResult previousCloseResult = AlgorithmResult.build(actualValue: 1000)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 1000)
		ProductResult previousResult = ProductResult.build(realTimeResult: previousRealTimeResult, closeResult: previousCloseResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 1001)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 999)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		Boolean isAllPositive = productResult.isAllPositive()

		then:
		!isAllPositive
	}

	void "test null previous run"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, diffUpperThreshold: 11, diffLowerThreshold: -11)
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
		Product product = Product.build(volatilePercentLimit: 20, diffUpperThreshold: 10, diffLowerThreshold: -10, realTimeDiffThreshold: 3, closeDiffThreshold: 5)
		AlgorithmResult twoPreviousCloseResult = AlgorithmResult.build(actualValue: 1000, predictedDifference: 5)
		AlgorithmResult twoPreviousRealTimeResult = AlgorithmResult.build(actualValue: 1000, predictedDifference: 5)
		ProductResult twoPreviousResult = ProductResult.build(realTimeResult: twoPreviousRealTimeResult, closeResult: twoPreviousCloseResult, product: product)
		AlgorithmResult previousCloseResult = AlgorithmResult.build(actualValue: 1011, predictedDifference: 5)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 1011, predictedDifference: 5)
		ProductResult previousResult = ProductResult.build(previousRun: twoPreviousResult, realTimeResult: previousRealTimeResult, closeResult: previousCloseResult, product: product)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 1022, predictedDifference: 5)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 1022, predictedDifference: 5)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.BUY
	}

	void "test both down"() {
		given:
		Product product = Product.build(volatilePercentLimit: 20, diffUpperThreshold: 10, diffLowerThreshold: -10, realTimeDiffThreshold: 3, closeDiffThreshold: 5)
		AlgorithmResult twoPreviousCloseResult = AlgorithmResult.build(actualValue: 1000, predictedDifference: -5)
		AlgorithmResult twoPreviousRealTimeResult = AlgorithmResult.build(actualValue: 1000, predictedDifference: -5)
		ProductResult twoPreviousResult = ProductResult.build(realTimeResult: twoPreviousRealTimeResult, closeResult: twoPreviousCloseResult, product: product)
		AlgorithmResult previousCloseResult = AlgorithmResult.build(actualValue: 989, predictedDifference: -5)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: 989, predictedDifference: -5)
		ProductResult previousResult = ProductResult.build(previousRun: twoPreviousResult, realTimeResult: previousRealTimeResult, closeResult: previousCloseResult, product: product)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 978, predictedDifference: -5)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 978, predictedDifference: -5)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.SELL
	}

	void "test too volatile"() {
		given:
		Product product = Product.build(volatilePercentLimit: 0.4, diffUpperThreshold: 11, diffLowerThreshold: -11)
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
		Product product = Product.build(volatilePercentLimit: 0.4, diffUpperThreshold: 11, diffLowerThreshold: -11)
		AlgorithmResult closeResult = AlgorithmResult.build()
		AlgorithmResult twoPreviousRealTimeResult = AlgorithmResult.build(actualValue: 1, predictedDifference: 5)
		ProductResult twoPreviousResult = ProductResult.build(realTimeResult: twoPreviousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult previousRealTimeResult = AlgorithmResult.build(actualValue: -15, predictedDifference: -5)
		ProductResult previousResult = ProductResult.build(previousRun: twoPreviousResult, realTimeResult: previousRealTimeResult, closeResult: closeResult, product: product)
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: -15, predictedDifference: -5)
		ProductResult productResult = ProductResult.build(previousRun: previousResult, realTimeResult: realTimeResult, closeResult: closeResult, product: product)

		when:
		RuleEvaluationAction action = productResult.action

		then:
		action == RuleEvaluationAction.HOLD
	}
}
