package com.augurworks.engine

import grails.transaction.Transactional
import java.security.SecureRandom

@Transactional
class DataGeneratorService {

	void importQuandlDataSets() {
		new URL('https://s3.amazonaws.com/quandl-static-content/quandl-stock-code-list.csv').getText().split('\n').tail().each { String line ->
			Collection<String> row = line.split(',')
			if (row[2] != 'NA' && row[4] == 'Active') {
				new DataSet(ticker: row[0], name: row[1], code: row[2], dataColumn: 4).save()
			}
		}
	}

	void generateRequest(int requestNumber) {
		Collection<DataSet> dataSets = DataSet.list()
		(1..requestNumber).each { int requestCount ->
			SecureRandom rand = new SecureRandom()
			Collection<DataSet> algorithmRequestDataSets = (0..4).collect {
				return dataSets[rand.nextInt(dataSets.size())]
			}
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(startDate: Date.parse('yyyy/MM', '2010/02'), endDate: Date.parse('yyyy/MM', '2015/04'), dependantDataSet: algorithmRequestDataSets[0]).save()
			algorithmRequestDataSets.each { DataSet dataSet ->
				new RequestDataSet(
					dataSet: dataSet,
					offset: 0,
					algorithmRequest: algorithmRequest
				).save()
			}
		}
	}
}
