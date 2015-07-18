package com.augurworks.engine.helper

import grails.test.mixin.TestFor
import spock.lang.Specification
import groovy.time.TimeCategory
import com.augurworks.engine.AugurWorksException

class RequestValueSetSpec extends Specification {

	static final String DATE_FORMAT = 'yyyy-MM-dd'

	Map validRequestValueSetParams(int valueCount) {
		return [
			name: 'Test Set',
			values: (0..(valueCount - 1)).collect { int i ->
				Date startDate = Date.parse(DATE_FORMAT, '2014-01-01')
				Date currentDate = use(TimeCategory) {
					startDate + i.days
				}
				return new DataSetValue(currentDate.format('yyyy-MM-dd'), i.toString())
			}
		]
	}

	void "test get dates"() {
		setup:
		Map validParams = validRequestValueSetParams(3)
		RequestValueSet set = new RequestValueSet(validParams.name, validParams.values)

		expect:
		set.dates == ['2014-01-01', '2014-01-02', '2014-01-03']
	}

	void "test filter values"() {
		setup:
		Map validParams = validRequestValueSetParams(10)
		Date startDate = Date.parse(DATE_FORMAT, start)
		Date endDate = Date.parse(DATE_FORMAT, end)
		RequestValueSet set = new RequestValueSet(validParams.name, validParams.values).filterValues(startDate, endDate, minOffset, maxOffset)

		expect:
		set.values.size() == size

		where:
		start        | end          | minOffset | maxOffset | size
		'2014-01-01' | '2014-01-09' | 0         | 0         | 9
		'2014-01-01' | '2014-01-01' | 0         | 0         | 1
		'2014-01-02' | '2014-01-09' | -1        | 1         | 10
	}

	void "test filter values exception"() {
		setup:
		Map validParams = validRequestValueSetParams(10)
		Date startDate = Date.parse(DATE_FORMAT, start)
		Date endDate = Date.parse(DATE_FORMAT, end)
		RequestValueSet set = new RequestValueSet(validParams.name, validParams.values)

		when:
		set.filterValues(startDate, endDate, minOffset, maxOffset)

		then:
		thrown(AugurWorksException)

		where:
		start        | end          | minOffset | maxOffset
		'2014-01-01' | '2014-01-10' | -1        | 0
		'2014-01-01' | '2014-01-10' | 0         | 1
		'2014-01-01' | '2014-01-10' | -1        | 1
	}
}