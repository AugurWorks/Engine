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
		dataSets.each { DataSet dataSet ->
			new RequestDataSet(
				dataSet: dataSet,
				offset: 0,
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
}
