package com.augurworks.engine.services

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import spock.lang.Specification

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.helper.Global

@Build([AlgorithmResult])
@Mock([AlgorithmResult, PredictedValue])
@TestFor(MachineLearningService)
class MachineLearningServiceSpec extends Specification {

	void "test parse prediction output file"() {
		given:
		File testFile = new File('src/test/resources/Example-Prediction.txt')

		when:
		Collection<Double> predictions = service.parsePredictionOutputFile(testFile)

		then:
		predictions.size() == 126
		predictions.collect { it != 0 }.every()
		predictions[0].value.round() == 355
		predictions[10].value.round() == 364
	}

	void "test create predicted values"() {
		given:
		AlgorithmResult algorithmResult = AlgorithmResult.build(dateCreated: new Date())
		Collection<Date> predictionDates = ['01/01/2014', '01/02/2014', '01/03/2014'].collect { String date ->
			return Date.parse(Global.DATE_FORMAT, date)
		}
		Collection<Double> predictions = [1.0, 1.0, 1.0, 1.0]

		when:
		service.createPredictedValues(algorithmResult, predictionDates, predictions)
		Collection<PredictedValue> predictedValues = algorithmResult.predictedValues

		then:
		predictedValues.size() == 4
		predictedValues*.value.sort() == predictions.sort()
		predictedValues*.date*.format(Global.DATE_FORMAT).contains('01/06/2014')
	}
}
