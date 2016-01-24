package com.augurworks.engine.services

import grails.test.mixin.*
import spock.lang.Specification

import com.augurworks.engine.helper.DataSetValue

@TestFor(DataRetrievalService)
class DataRetrievalServiceSpec extends Specification {

	void "test construct Google url"() {
		when:
		String url = service.constructGoogleUrl('GOOG', Date.parse('MM/dd/yyyy', '01/20/2016'), Date.parse('MM/dd/yyyy', '01/22/2016'), 30)

		then:
		url.indexOf('p=3') != -1
		url.indexOf('q=GOOG') != -1
	}

	void "test parse Google data"() {
		given:
		Date startDate = Date.parse('MM/dd/yyyy', '01/20/2016')
		startDate.set(minute: 570)

		when:
		DataSetValue dataSetValue = service.parseGoogleData(startDate, 60, row)

		then:
		dataSetValue.date.format('HH:mm') == time
		dataSetValue.value.toInteger() == intValue

		where:
		row                 | time    | intValue
		'a1453300200,689.5' | '09:30' | 689
		'1,100'             | '10:30' | 100
		'48,701.22'         | '09:30' | 701
	}
}
