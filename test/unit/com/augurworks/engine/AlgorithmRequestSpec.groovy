package com.augurworks.engine

import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(AlgorithmRequest)
@Mock([DataSet, RequestDataSet])
class AlgorithmRequestSpec extends Specification {

	static final String DATE_FORMAT = 'yyyy-MM-dd'

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
		Date startDate = Date.parse(DATE_FORMAT, '2014-01-01')
		Date endDate = Date.parse(DATE_FORMAT, '2015-01-01')
		Date dateCreated = Date.parse(DATE_FORMAT, '2015-01-01')
		Collection<DataSet> dataSets = requestDataSetRange.collect { int id ->
			return new DataSet(dataSetParams(id)).save()
		}
		AlgorithmRequest algorithmRequest = new AlgorithmRequest(
			startDate: startDate,
			endDate: endDate,
			dateCreated: dateCreated,
			dependantDataSet: dataSets.first()
		)
		algorithmRequest.save()
		dataSets.eachWithIndex { DataSet dataSet, int counter ->
			new RequestDataSet(
				dataSet: dataSet,
				offset: counter,
				algorithmRequest: algorithmRequest
			).save()
		}
		return algorithmRequest
	}

	void "test create algorithm request"() {
		when:
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))

		then:
		algorithmRequest.id == 1
		AlgorithmRequest.count() == 1
		algorithmRequest.requestDataSets.size() == 4
	}

	void "test update fields algorithm request"() {
		given:
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))
		DataSet dataSet = new DataSet(dataSetParams(1)).save()
		Map parameters = [
			endDate: Date.parse(DATE_FORMAT, end),
			dependantDataSet: dataSet
		]

		when:
		algorithmRequest.updateFields(parameters)

		then:
		algorithmRequest.endDate.format(DATE_FORMAT) == end
		algorithmRequest.dependantDataSet.id == dataSet.id

		where:
		end          | dataSetCount
		'2015-02-01' | 4
		'2015-02-01' | 7
		'2014-02-01' | 4
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
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..(size - 1)))
		RequestDataSet requestDataSet = algorithmRequest.requestDataSets[requestDataSetNum]

		when:
		algorithmRequest.dependantDataSet = requestDataSet.dataSet
		algorithmRequest.save()

		then:
		algorithmRequest.independentRequestDataSets.size() == size - 1
		!(requestDataSet in algorithmRequest.independentRequestDataSets)

		where:
		requestDataSetNum | size
		0                 | 1
		0                 | 2
		1                 | 2
	}

	void "test get prediction offset"() {
		given:
		AlgorithmRequest algorithmRequest = validAlgorithmRequest((0..3))
		RequestDataSet requestDataSet = algorithmRequest.requestDataSets[requestDataSetNum]
		int predictionOffset = requestDataSet.offset

		when:
		algorithmRequest.dependantDataSet = requestDataSet.dataSet
		algorithmRequest.save()

		then:
		predictionOffset == algorithmRequest.predictionOffset

		where:
		requestDataSetNum << [0, 1, 2, 3]
	}
}
