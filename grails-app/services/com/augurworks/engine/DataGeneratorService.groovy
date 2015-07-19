package com.augurworks.engine

import grails.transaction.Transactional

@Transactional
class DataGeneratorService {

	static final Collection<String> VALID_TICKERS = [
		'AAPL',
		'GOOGL',
		'JPM',
		'USO',
		'AMZN',
		'FB',
		'TWTR',
		'YHOO',
		'MSFT'
	]

	void importQuandlDataSets() {
		new URL('https://s3.amazonaws.com/quandl-static-content/quandl-stock-code-list.csv').getText().split('\n').tail().each { String line ->
			Collection<String> row = line.split(',');
			if (row[2] != 'NA' && row[4] == 'Active') {
				new DataSet(ticker: row[0], name: row[1], code: row[2], dataColumn: 4).save();
			}
		}
	}

	void generateRequest(int requestNumber) {
		Collection<DataSet> dataSets = DataSet.list()
		(1..requestNumber).each { int requestCount ->
			Random rand = new Random()
			Collection<String> tickers = VALID_TICKERS
			Collection<DataSet> algorithmRequestDataSets = (0..3).collect {
				
				String ticker = tickers[rand.nextInt(tickers.size())]
				tickers -= ticker
				return DataSet.findByTicker(ticker)
			}
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(startDate: Date.parse('yyyy/MM', '2014/12'), endDate: Date.parse('yyyy/MM', '2015/06'), dependantDataSet: algorithmRequestDataSets[0]).save()
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
