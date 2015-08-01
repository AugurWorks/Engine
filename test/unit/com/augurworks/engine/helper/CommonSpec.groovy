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
}
