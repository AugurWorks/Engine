package com.augurworks.engine.services

import grails.transaction.Transactional
import groovy.time.TimeCategory

import java.security.SecureRandom

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Aggregations

@Transactional
class DataGeneratorService {

	GrailsApplication grailsApplication

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
			Collection<String> row = line.split(',')
			if (row[2] != 'NA' && row[4] == 'Active') {
				new DataSet(ticker: row[0], name: row[1], code: row[2], dataColumn: 4).save()
			}
		}
	}

	void importLocalDataSets() {
		grailsApplication.mainContext.getResource('data/Extra-Data-Sources.csv').file.text.split('\n').tail().each { String line ->
			Collection<String> row = line.split(',')
			new DataSet(ticker: row[0], name: row[1], code: row[2], dataColumn: row[3]).save()
		}
	}

	void generateRequest(int requestNumber) {
		(1..requestNumber).each { int requestCount ->
			SecureRandom rand = new SecureRandom()
			Collection<String> tickers = VALID_TICKERS
			Collection<DataSet> algorithmRequestDataSets = (0..5).collect {
				String ticker = tickers[rand.nextInt(tickers.size())]
				tickers -= ticker
				return DataSet.findByTicker(ticker)
			}
			int startOffset = use(TimeCategory) { (Date.parse('yyyy/MM', '2015/06') - new Date()).days }
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(startOffset: startOffset, endOffset: startOffset + 28, dependantDataSet: algorithmRequestDataSets[0]).save()
			algorithmRequestDataSets.eachWithIndex { DataSet dataSet, int counter ->
				new RequestDataSet(
					dataSet: dataSet,
					offset: counter == 0 ? 0 : -1,
					aggregation: Aggregations.TYPES[rand.nextInt(Aggregations.TYPES.size())],
					algorithmRequest: algorithmRequest
				).save()
			}
		}
	}
}
