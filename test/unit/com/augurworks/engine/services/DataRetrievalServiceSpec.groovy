package com.augurworks.engine.services

import grails.test.mixin.*
import spock.lang.Specification

@TestFor(DataRetrievalService)
class DataRetrievalServiceSpec extends Specification {

	void "test construct Google url"() {
		when:
		String url = service.constructGoogleUrl('GOOG', Date.parse('MM/dd/yyyy', '01/20/2016'), Date.parse('MM/dd/yyyy', '01/22/2016'), 30)

		then:
		url.indexOf('p=3') != -1
		url.indexOf('q=GOOG') != -1
	}
}
