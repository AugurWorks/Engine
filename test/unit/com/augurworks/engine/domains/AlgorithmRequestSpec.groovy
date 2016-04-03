package com.augurworks.engine.domains

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import groovy.time.TimeCategory
import spock.lang.Specification

import com.augurworks.engine.helper.Aggregation

@TestFor(AlgorithmRequest)
@Build([AlgorithmRequest, RequestDataSet])
@Mock([RequestDataSet])
class AlgorithmRequestSpec extends Specification {

	static final String DATE_FORMAT = 'yyyy-MM-dd'
	static final String DATE_TIME_FORMAT = 'yyyy-MM-dd HH:mm'

	int dateToOffset(String dateString) {
		return dateToOffset(Date.parse(DATE_FORMAT, dateString))
	}

	int dateToOffset(Date date) {
		return use(TimeCategory) { (date - new Date()).days }
	}

	void "test create algorithm request"() {
		when:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build()

		then:
		algorithmRequest.id == 1
		AlgorithmRequest.count() == 1
	}

	void "test truncate date (hour)"() {
		given:
		Date mockTime = Date.parse(DATE_TIME_FORMAT, startTime)
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(startOffset: offset, unit: 'Hour')

		when:
		Date startDate = algorithmRequest.truncateDate('startOffset', mockTime)

		then:
		startDate == Date.parse(DATE_TIME_FORMAT, expectedTime)

		where:
		startTime          | offset | expectedTime
		'2016-01-10 14:00' | 0      | '2016-01-10 14:00'
		'2016-01-10 14:00' | -5     | '2016-01-10 09:00'
		'2016-01-10 14:29' | -7     | '2016-01-10 07:00'
		'2016-01-10 14:00' | -13    | '2016-01-10 01:00'
		'2016-01-10 14:00' | 5      | '2016-01-10 19:00'
		'2016-01-10 14:40' | 5      | '2016-01-10 19:30'
		'2016-01-10 14:30' | -4     | '2016-01-10 10:30'
	}

	void "test truncate date (day)"() {
		given:
		Date mockTime = Date.parse(DATE_FORMAT, '2016-01-10')
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(startOffset: offset, unit: 'Day')

		when:
		Date startDate = algorithmRequest.truncateDate('startOffset', mockTime)

		then:
		startDate == Date.parse(DATE_FORMAT, expectedTime)

		where:
		offset | expectedTime
		0      | '2016-01-10'
		10     | '2016-01-20'
		-5     | '2016-01-05'
	}

	void "test update fields algorithm request"() {
		given:
		RequestDataSet requestDataSet = RequestDataSet.build(symbol: 'Test')
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(requestDataSets: [requestDataSet])
		Map parameters = [
			endOffset: end,
			dependantSymbol: requestDataSet.symbol
		]

		when:
		algorithmRequest.updateFields(parameters)

		then:
		algorithmRequest.endOffset == end
		algorithmRequest.dependentRequestDataSet == requestDataSet

		where:
		end  | dataSetCount
		-23  | 4
		-55  | 7
		-100 | 4
	}

	void "test update data sets to algorithm request"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(requestDataSets: (0..1).collect { RequestDataSet.build() })
		Collection<RequestDataSet> requestDataSets = (1..dataSetCount).collect { RequestDataSet.build() }

		when:
		algorithmRequest.updateDataSets(requestDataSets)

		then:
		algorithmRequest.requestDataSets.size() == dataSetCount

		where:
		dataSetCount << [4, 7, 3]
	}

	void "test get independent request data sets"() {
		given:
		int counter = 0
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(requestDataSets: (0..(size - 1)).collect { RequestDataSet.build(symbol: 'Symbol ' + it) })
		RequestDataSet requestDataSet = algorithmRequest.requestDataSets[requestDataSetNum]
		algorithmRequest.metaClass.getDependentRequestDataSet = { counter++; return requestDataSet }

		when:
		algorithmRequest.dependantSymbol = requestDataSet.symbol
		algorithmRequest.save()
		Collection<RequestDataSet> independent = algorithmRequest.independentRequestDataSets

		then:
		independent.size() == size - 1
		!(requestDataSet in independent)
		counter == 1

		where:
		requestDataSetNum | size
		0                 | 1
		0                 | 2
		1                 | 2
	}

	void "test get prediction offset"() {
		given:
		int counter = 0
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(requestDataSets: (0..3).collect { RequestDataSet.build() })
		algorithmRequest.metaClass.getDependentRequestDataSet = { counter++; return [offset: 10] }

		when:
		int offset = algorithmRequest.predictionOffset

		then:
		offset == 10
		counter == 1

		where:
		requestDataSetNum << [0, 1, 2, 3]
	}

	void "get dependent request data set"() {
		given:
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(requestDataSets: (0..3).collect { RequestDataSet.build(symbol: 'Symbol ' + it) })
		RequestDataSet requestDataSet = algorithmRequest.requestDataSets[requestDataSetNum]

		when:
		algorithmRequest.dependantSymbol = requestDataSet.symbol
		algorithmRequest.save()

		then:
		requestDataSet == algorithmRequest.dependentRequestDataSet

		where:
		requestDataSetNum << [0, 1, 2, 3]
	}
}
