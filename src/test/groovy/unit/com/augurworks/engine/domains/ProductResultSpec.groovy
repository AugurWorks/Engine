package unit.com.augurworks.engine.domains

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.ProductResult
import grails.buildtestdata.mixin.Build
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.Specification

@Mock([AlgorithmResult,ProductResult])
@Build([AlgorithmResult, ProductResult])
@TestFor(ProductResult)
class ProductResultSpec extends Specification {

	void "test is all positive"() {
		given:
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: 1)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 1)
		ProductResult productResult = ProductResult.build(realTimeResult: realTimeResult, closeResult: closeResult)

		when:
		Boolean isAllPositive = productResult.allPositive

		then:
		isAllPositive
	}

	void "test is not all positive"() {
		given:
		AlgorithmResult realTimeResult = AlgorithmResult.build(actualValue: -1)
		AlgorithmResult closeResult = AlgorithmResult.build(actualValue: 1)
		ProductResult productResult = ProductResult.build(realTimeResult: realTimeResult, closeResult: closeResult)

		when:
		Boolean isAllPositive = productResult.allPositive

		then:
		!isAllPositive
	}
}
