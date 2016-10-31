package com.augurworks.engine.helper

import groovy.time.TimeCategory
import spock.lang.Specification

import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.model.DataSetValue
import com.augurworks.engine.model.RequestValueSet

class RequestValueSetSpec extends Specification {

	static final Collection<String> TEST_DATES = ['01/01/2014', '01/02/2014', '01/03/2014']

	RequestValueSet validRequestValueSet(int valueCount, int offset = 0) {
		Collection<String> dates = generateDates(valueCount)*.format(Global.DATE_FORMAT)
		return validRequestValueSet(dates, offset)
	}

	RequestValueSet validRequestValueSet(Collection<String> dates, int offset = 0) {
		Collection<Date> nonStringDates = dates.collect { String date ->
			return Date.parse(Global.DATE_FORMAT, date)
		}
		int i = 0
		Map validParams = [
			name: 'Test Set',
			offset: offset,
			values: nonStringDates.collect { Date date ->
				i++
				return new DataSetValue(date, i.toString().toDouble())
			}
		]
		return new RequestValueSet(validParams.name, DataType.CLOSE, validParams.offset, validParams.values)
	}

	Collection<Date> generateDates(int valueCount) {
		return (0..(valueCount - 1)).collect { int i ->
			Date startDate = Date.parse(Global.DATE_FORMAT, '01/01/2014')
			use(TimeCategory) {
				return startDate + i.days
			}
		}
	}

	Collection<Date> stringsToDates(Collection<String> dates) {
		return dates.collect { String date ->
			return Date.parse(Global.DATE_FORMAT, date)
		}
	}

	void "test get dates"() {
		setup:
		RequestValueSet set = validRequestValueSet(3)

		expect:
		set.dates*.format(Global.DATE_FORMAT) == TEST_DATES
	}

	void "test aggregate values"() {
		given:
		RequestValueSet set = validRequestValueSet(10)

		when:
		set.aggregateValues(aggregationType)

		then:
		set.values.size() == valuesSize

		where:
		aggregationType                   | valuesSize
		Aggregation.VALUE                 | 10
		Aggregation.PERIOD_CHANGE         | 9
		Aggregation.PERIOD_PERCENT_CHANGE | 9
	}

	void "test filter values"() {
		setup:
		Date startDate = Date.parse(Global.DATE_FORMAT, start)
		Date endDate = Date.parse(Global.DATE_FORMAT, end)
		RequestValueSet set = validRequestValueSet(10).filterValues(Unit.DAY, startDate, endDate, minOffset, maxOffset)

		expect:
		set.values.size() == size

		where:
		start        | end          | minOffset | maxOffset | size
		'01/01/2014' | '01/09/2014' | 0         | 0         | 9
		'01/01/2014' | '01/01/2014' | 0         | 0         | 1
		'01/02/2014' | '01/09/2014' | -1        | 1         | 10
	}

	void "test filter values exception"() {
		setup:
		Date startDate = Date.parse(Global.DATE_FORMAT, start)
		Date endDate = Date.parse(Global.DATE_FORMAT, end)
		RequestValueSet set = validRequestValueSet(10)

		when:
		set.filterValues(Unit.DAY, startDate, endDate, minOffset, maxOffset)

		then:
		thrown(AugurWorksException)

		where:
		start        | end          | minOffset | maxOffset
		'01/01/2014' | '01/10/2014' | -1        | 0
		'01/01/2014' | '01/10/2014' | 0         | 1
		'01/01/2014' | '01/10/2014' | -1        | 1
	}

	void "test fill out values"() {
		setup:
		Collection<String> dates = TEST_DATES - TEST_DATES[1..1]
		RequestValueSet set = validRequestValueSet(dates)

		when:
		Collection<DataSetValue> values = set.fillOutValues(stringsToDates(TEST_DATES)).values

		then:
		values.size() == TEST_DATES.size()
		values*.value == [1, 1, 2]
	}

	void "test fill out values exception empty dates"() {
		setup:
		RequestValueSet set = validRequestValueSet(TEST_DATES)

		when:
		set.fillOutValues([])

		then:
		thrown(AugurWorksException)
	}

	void "test reduce value range with prediction"() {
		setup:
		Date startDate = Date.parse(Global.DATE_FORMAT, start)
		Date endDate = Date.parse(Global.DATE_FORMAT, end)
		RequestValueSet set = validRequestValueSet(10, offset)

		when:
		Collection<Date> dates = set.reduceValueRange(Unit.DAY, startDate, endDate, predictionOffset).dates

		then:
		dates.size() == size
		dates.first().format(Global.DATE_FORMAT) == first
		dates.last().format(Global.DATE_FORMAT) == last

		where:
		start        | end          | offset | predictionOffset | size | first        | last
		'01/01/2014' | '01/10/2014' | 0      | 0                | 10   | '01/01/2014' | '01/10/2014'
		'01/02/2014' | '01/09/2014' | 1      | -1               | 10   | '01/01/2014' | '01/10/2014'
		'01/02/2014' | '01/09/2014' | -1     | 0                | 9    | '01/01/2014' | '01/09/2014'
		'01/03/2014' | '01/05/2014' | 5      | -2               | 10   | '01/01/2014' | '01/10/2014'
	}

	void "test reduce value range"() {
		setup:
		Date startDate = Date.parse(Global.DATE_FORMAT, start)
		Date endDate = Date.parse(Global.DATE_FORMAT, end)
		RequestValueSet set = validRequestValueSet(10, offset)

		when:
		Collection<Date> dates = set.reduceValueRange(Unit.DAY, startDate, endDate).dates

		then:
		dates.size() == size
		dates.first().format(Global.DATE_FORMAT) == first
		dates.last().format(Global.DATE_FORMAT) == last

		where:
		start        | end          | offset | size | first        | last
		'01/01/2014' | '01/10/2014' | 0      | 10   | '01/01/2014' | '01/10/2014'
		'01/01/2014' | '01/09/2014' | 1      | 9    | '01/02/2014' | '01/10/2014'
		'01/02/2014' | '01/10/2014' | -1     | 9    | '01/01/2014' | '01/09/2014'
		'01/02/2014' | '01/05/2014' | 5      | 4    | '01/07/2014' | '01/10/2014'
	}
}