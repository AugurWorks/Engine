package com.augurworks.engine.helper

import grails.test.mixin.*
import spock.lang.Specification

class CommonSpec extends Specification {

	void "test next weekday"() {
		given:
		Date startDate = Date.parse('MM/dd/yyyy', start)

		when:
		Date nextWeekdayDate = Common.nextWeekday(startDate)

		then:
		nextWeekdayDate.format('MM/dd/yyyy') == nextWeekday

		where:
		start        | nextWeekday
		'09/19/2015' | '09/21/2015'
		'09/20/2015' | '09/21/2015'
		'09/21/2015' | '09/21/2015'
	}
}
