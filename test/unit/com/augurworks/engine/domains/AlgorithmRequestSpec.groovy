package com.augurworks.engine.domains

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import groovy.time.TimeCategory
import spock.lang.Specification

import com.augurworks.engine.helper.Aggregations

@TestFor(AlgorithmRequest)
@Build([AlgorithmRequest])
@Mock([DataSet, RequestDataSet])
class AlgorithmRequestSpec extends Specification {

	static final String DATE_FORMAT = 'yyyy-MM-dd'
	static final String DATE_TIME_FORMAT = 'yyyy-MM-dd HH:mm'

	Map dataSetParams(int num) {
		return [
			ticker: 'XX' + num + 'XX',
			name: 'Stock ' + num,
			code: 'xxxx',
			dataColumn: 4
		]
	}

	Map requestDataSetParams(int num) {
		return [
			dataSet: DataSet.get(num),
			offset: num
		]
	}

	AlgorithmRequest validAlgorithmRequest(Collection<Integer> requestDataSetRange) {
		int startOffset = dateToOffset('2014-01-01')
		int endOffset = dateToOffset('2015-01-01')
		Date dateCreated = Date.parse(DATE_FORMAT, '2015-01-01')
		Collection<DataSet> dataSets = requestDataSetRange.collect { int id ->
			return new DataSet(dataSetParams(id)).save()
		}
		AlgorithmRequest algorithmRequest = new AlgorithmRequest(
			startOffset: startOffset,
			endOffset: endOffset,
			dateCreated: dateCreated,
			dependantDataSet: dataSets.first()
		)
		algorithmRequest.save()
		dataSets.eachWithIndex { DataSet dataSet, int counter ->
			new RequestDataSet(
				dataSet: dataSet,
				offset: counter,
				aggregation: Aggregations.TYPES[0],
				algorithmRequest: algorithmRequest
			).save()
		}
		return algorithmRequest
	}

	int dateToOffset(String dateString) {
		return dateToOffset(Date.parse(DATE_FORMAT, dateString))
	}

	int dateToOffset(Date date) {
		return use(TimeCategory) { (date - new Date()).days }
	}

	void "test create algorithm request"() {
		when:
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))

		then:
		algorithmRequest.id == 1
		AlgorithmRequest.count() == 1
	}

	void "test truncate date (hour)"() {
		given:
		Date mockTime = Date.parse(DATE_TIME_FORMAT, '2016-01-10 14:00')
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(startOffset: offset, unit: 'Hour')
		algorithmRequest.metaClass.now = { return mockTime }

		when:
		Date startDate = algorithmRequest.truncateDate('startOffset')

		then:
		startDate == Date.parse(DATE_TIME_FORMAT, expectedTime)

		where:
		offset | expectedTime
		0      | '2016-01-10 14:00'
		-5     | '2016-01-10 09:00'
		-7     | '2016-01-10 07:00'
		-13    | '2016-01-10 01:00'
		5      | '2016-01-10 19:00'
	}

	void "test truncate date (day)"() {
		given:
		Date mockTime = Date.parse(DATE_FORMAT, '2016-01-10')
		AlgorithmRequest algorithmRequest = AlgorithmRequest.build(startOffset: offset, unit: 'Day')
		algorithmRequest.metaClass.now = { return mockTime }

		when:
		Date startDate = algorithmRequest.truncateDate('startOffset')

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
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))
		DataSet dataSet = new DataSet(dataSetParams(1)).save()
		Map parameters = [
			endOffset: end,
			dependantDataSet: dataSet
		]

		when:
		algorithmRequest.updateFields(parameters)

		then:
		algorithmRequest.endOffset == end
		algorithmRequest.dependantDataSet.id == dataSet.id

		where:
		end  | dataSetCount
		-23  | 4
		-55  | 7
		-100 | 4
	}

	void "test update data sets to algorithm request"() {
		given:
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))
		Collection<DataSet> dataSets = (0..(dataSetCount - 1)).collect { int id ->
			return new DataSet(dataSetParams(id)).save()
		}
		Collection<Map> dataSetMaps = dataSets.collect { DataSet dataSet ->
			return [
				name: dataSet.ticker,
				aggregation: Aggregations.TYPES[0],
				offset: 0
			]
		}

		when:
		algorithmRequest.updateDataSets(dataSetMaps)

		then:
		algorithmRequest.requestDataSets.size() == dataSetCount

		where:
		dataSetCount << [4, 7, 3]
	}

	void "test get independent request data sets"() {
		given:
		int counter = 0
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..(size - 1)))
		RequestDataSet requestDataSet = algorithmRequest.requestDataSets[requestDataSetNum]
		algorithmRequest.metaClass.getDependentRequestDataSet = { counter++; return requestDataSet }

		when:
		algorithmRequest.dependantDataSet = requestDataSet.dataSet
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
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))
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
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))
		RequestDataSet requestDataSet = algorithmRequest.requestDataSets[requestDataSetNum]

		when:
		algorithmRequest.dependantDataSet = requestDataSet.dataSet
		algorithmRequest.save()

		then:
		requestDataSet == algorithmRequest.dependentRequestDataSet

		where:
		requestDataSetNum << [0, 1, 2, 3]
	}
}
