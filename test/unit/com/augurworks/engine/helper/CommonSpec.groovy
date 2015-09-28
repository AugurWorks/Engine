package com.augurworks.engine.helper

import grails.test.mixin.*
import spock.lang.Specification

class CommonSpec extends Specification {

	void "test add date"() {
		given:
		Date start = Date.parse(Global.DATE_FORMAT, startDate)

		when:
		Date result = Common.addDaysToDate(start, offset)

		then:
		result.format(Global.DATE_FORMAT) == resultDate

		where:
		startDate    | resultDate   | offset
		'01/01/2014' | '01/02/2014' | 1
		'01/01/2014' | '02/01/2014' | 31
		'01/01/2014' | '12/31/2013' | -1
		'01/02/2014' | '01/01/2014' | -1
		'01/01/2014' | '01/11/2014' | 10
	}

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
