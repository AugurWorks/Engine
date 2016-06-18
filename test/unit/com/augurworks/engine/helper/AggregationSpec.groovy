package com.augurworks.engine.helper

import grails.test.mixin.*
import spock.lang.Specification

import com.augurworks.engine.exceptions.AugurWorksException

class AggregationSpec extends Specification {

	void "test valid period percent change"() {
		when:
		double result = Aggregation.periodPercentChange(previousValue, currentValue)

		then:
		Math.abs(expected - result) < 1

		where:
		previousValue | currentValue | expected
		1D            | 2D           | 100D
		2D            | 1D           | -50D
		5D            | 0D           | -100D
	}

	void "test invalid period percent change"() {
		when:
		Aggregation.periodPercentChange(0, 100)

		then:
		thrown(AugurWorksException)
	}

	void "test valid period percent change normalize"() {
		when:
		double result = Aggregation.periodPercentChangeNormalize(previousValue, currentValue)

		then:
		Math.abs(expected - result) < 0.01

		where:
		previousValue | currentValue | expected
		1D            | 10D          | 1.1D
		2D            | -20D         | 1.60D
		5D            | 0D           | 5D
	}
}
