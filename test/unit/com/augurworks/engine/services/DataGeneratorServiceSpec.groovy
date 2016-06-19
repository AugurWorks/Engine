package com.augurworks.engine.services

import grails.test.mixin.*
import spock.lang.Specification

import com.augurworks.engine.model.DataSetValue;

@TestFor(DataGeneratorService)
class DataGeneratorServiceSpec extends Specification {

	void "test generate intra-day data"() {
		given:
		service.metaClass.generateRandomSeed = { String seedTicker ->
			return 1
		}

		when:
		Collection<DataSetValue> dataSets = service.generateIntraDayData(ticker, new Date(), days, intervalLength)

		then:
		dataSets.size() == expectedLength
		dataSets[0].value != dataSets[1].value
		dataSets[0].date != dataSets[1].date

		where:
		ticker | days | intervalLength | expectedLength
		'AAPL' | 1    | 30             | 14
		'AAPL' | 1    | 15             | 27
		'AAPL' | 10   | 30             | 140
	}

	void "test generate random seed"() {
		when:
		int seed = service.generateRandomSeed(ticker)

		then:
		seed == expectedSeed

		where:
		ticker | expectedSeed
		'A'    | 1
		'AB'   | 102
		'ZAB'  | 260102
	}
}
