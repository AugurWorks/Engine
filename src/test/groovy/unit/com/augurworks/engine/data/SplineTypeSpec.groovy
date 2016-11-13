package com.augurworks.engine.data

import grails.test.mixin.*
import spock.lang.Specification

class SplineTypeSpec extends Specification {

	Collection<Collection<Date>> allDates = [[
		'01/01/2014',
		'01/04/2014',
		'01/05/2014'
	], [
		'01/01/2014',
		'01/04/2014',
		'01/05/2014',
		'01/06/2014'
	]].collect { Collection<String> dates ->
		return dates.collect { String date ->
			return Date.parse('MM/dd/yyyy', date)
		}
	}

	void "test unique"() {
		given:
		Collection<Date> expectedDates = [
			'01/01/2014',
			'01/04/2014',
			'01/05/2014',
			'01/06/2014'
		].collect { String date ->
			return Date.parse('MM/dd/yyyy', date)
		}

		when:
		Collection<Date> dates = SplineType.unique(allDates)

		then:
		dates == expectedDates
	}

	void "test all contain dates"() {
		given:
		Collection<Date> expectedDates = [
			'01/01/2014',
			'01/04/2014',
			'01/05/2014'
		].collect { String date ->
			return Date.parse('MM/dd/yyyy', date)
		}

		when:
		Collection<Date> dates = SplineType.allContainDate(allDates)

		then:
		dates == expectedDates
	}
}
