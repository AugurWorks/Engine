package com.augurworks.engine

import grails.test.mixin.*
import spock.lang.Specification

@TestFor(MachineLearningService)
class MachineLearningServiceSpec extends Specification {

	void "test parse prediction output file"() {
		given:
		File testFile = new File('test/resources/Example-Prediction.txt')

		when:
		Collection<Double> predictions = service.parsePredictionOutputFile(testFile)

		then:
		predictions.size() == 126
		predictions.collect { it != 0 }.every()
	}
}
