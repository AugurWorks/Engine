package com.augurworks.engine.helper

import grails.test.mixin.TestFor
import spock.lang.Specification
import groovy.time.TimeCategory
import com.augurworks.engine.AugurWorksException

class RequestValueSetSpec extends Specification {

	static final String DATE_FORMAT = 'yyyy-MM-dd'
	static final Collection<String> TEST_DATES = ['2014-01-01', '2014-01-02', '2014-01-03']

	RequestValueSet validRequestValueSet(int valueCount, int offset = 0) {
		Collection<String> dates = generateDates(valueCount)
		return validRequestValueSet(dates, offset)
	}

	RequestValueSet validRequestValueSet(Collection<String> dates, int offset = 0) {
		int i = -1
		Map validParams = [
			name: 'Test Set',
			offset: offset,
			values: dates.collect { String date ->
				i++
				return new DataSetValue(date, i.toString())
			}
		]
		return new RequestValueSet(validParams.name, validParams.offset, validParams.values)
	}

	Collection<String> generateDates(int valueCount) {
		return (0..(valueCount - 1)).collect { int i ->
			Date startDate = Date.parse(DATE_FORMAT, '2014-01-01')
			return use(TimeCategory) {
				startDate + i.days
			}.format('yyyy-MM-dd')
		}
	}

	void "test get dates"() {
		setup:
		RequestValueSet set = validRequestValueSet(3)

		expect:
		set.dates == TEST_DATES
	}

	void "test filter values"() {
		setup:
		Date startDate = Date.parse(DATE_FORMAT, start)
		Date endDate = Date.parse(DATE_FORMAT, end)
		RequestValueSet set = validRequestValueSet(10).filterValues(startDate, endDate, minOffset, maxOffset)

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
		Date startDate = Date.parse(DATE_FORMAT, start)
		Date endDate = Date.parse(DATE_FORMAT, end)
		RequestValueSet set = validRequestValueSet(10)

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

	void "test fill out values"() {
		setup:
		Collection<String> dates = TEST_DATES - TEST_DATES[1..1]
		RequestValueSet set = validRequestValueSet(dates)

		when:
		Collection<DataSetValue> values = set.fillOutValues(TEST_DATES).values

		then:
		values.size() == TEST_DATES.size()
		values*.value == [0, 0, 1]
	}

	void "test fill out values exception"() {
		setup:
		Collection<String> dates = TEST_DATES - TEST_DATES[0..0]
		RequestValueSet set = validRequestValueSet(dates)

		when:
		set.fillOutValues(TEST_DATES)

		then:
		thrown(AugurWorksException)
	}

	void "test fill out values exception empty dates"() {
		setup:
		RequestValueSet set = validRequestValueSet(TEST_DATES)

		when:
		set.fillOutValues([])

		then:
		thrown(AugurWorksException)
	}

	void "test reduce value range"() {
		setup:
		Date startDate = Date.parse(DATE_FORMAT, start)
		Date endDate = Date.parse(DATE_FORMAT, end)
		RequestValueSet set = validRequestValueSet(10, offset)

		when:
		Collection<String> dates = set.reduceValueRange(startDate, endDate).dates

		then:
		dates.size() == size
		dates.first() == first
		dates.last() == last

		where:
		start        | end          | offset | size | first        | last
		'2014-01-01' | '2014-01-10' | 0      | 10   | '2014-01-01' | '2014-01-10'
		'2014-01-01' | '2014-01-09' | 1      | 9    | '2014-01-02' | '2014-01-10'
		'2014-01-02' | '2014-01-10' | -1     | 9    | '2014-01-01' | '2014-01-09'
		'2014-01-02' | '2014-01-05' | 5      | 4    | '2014-01-07' | '2014-01-10'
	}
}