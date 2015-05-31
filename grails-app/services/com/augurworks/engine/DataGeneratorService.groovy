package com.augurworks.engine

import grails.transaction.Transactional

@Transactional
class DataGeneratorService {

	void importQuandlDataSets() {
		new URL('https://s3.amazonaws.com/quandl-static-content/quandl-stock-code-list.csv').getText().split('\n').tail().each { String line ->
			Collection<String> row = line.split(',');
			if (row[2] != 'NA' && row[4] == 'Active') {
				new DataSet(ticker: row[0], name: row[1], code: row[2], dataColumn: 4).save();
			}
		}
	}

	void generateRequest(int requestNumber) {
		Collection<DataSet> dataSets = DataSet.list();
		(1..requestNumber).each { int requestCount ->
			Random rand = new Random();
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(startDate: Date.parse('yyyy/MM', '2010/02'), endDate: Date.parse('yyyy/MM', '2015/04')).save();
			(0..4).each {
				new RequestDataSet(
					dataSet: dataSets[rand.nextInt(dataSets.size())],
					offset: 0,
					algorithmRequest: algorithmRequest
				).save();
			}
		}
	}
}
