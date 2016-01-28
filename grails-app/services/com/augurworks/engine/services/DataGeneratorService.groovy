package com.augurworks.engine.services

import grails.transaction.Transactional
import groovy.time.TimeCategory

import java.security.SecureRandom

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Aggregations
import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.RequestValueSet

@Transactional
class DataGeneratorService {

	GrailsApplication grailsApplication

	static final Collection<Map> DEFAULT_REQUESTS = [[
		tickers: ['AAPL', 'BAC', 'GE', 'GOOG', 'GS', 'GSPC', 'JPM', 'ORCL', 'USO'],
		dependent: 'GSPC',
		startOffset: -15,
		endOffset: -1
	], [
		tickers: ['AAPL', 'BAC', 'GE', 'GOOG', 'GS', 'GSPC', 'JPM', 'ORCL', 'USO'],
		dependent: 'GSPC',
		startOffset: -22,
		endOffset: -1
	], [
		tickers: ['AAPL', 'BAC', 'GE', 'GOOG', 'GS', 'GSPC', 'JPM', 'ORCL', 'USO'],
		dependent: 'GSPC',
		startOffset: -29,
		endOffset: -1
	]]

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

	void bootstrapDefaultRequests() {
		DEFAULT_REQUESTS.each { Map requestMap ->
			Collection<DataSet> algorithmRequestDataSets = requestMap.tickers.collect { String ticker ->
				return DataSet.findByTicker(ticker)
			}
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(startOffset: requestMap.startOffset, endOffset: requestMap.endOffset, dependantDataSet: DataSet.findByTicker(requestMap.dependent)).save()
			algorithmRequestDataSets.each { DataSet dataSet ->
				new RequestDataSet(
					dataSet: dataSet,
					offset: dataSet.ticker == requestMap.dependent ? 0 : -1,
					aggregation: 'Period Percent Change',
					algorithmRequest: algorithmRequest
				).save()
			}
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

	RequestValueSet generateRequestSet(String ticker) {
		int days = 5
		Date startDate = use (TimeCategory) { new Date() - days.days }
		Collection<DataSetValue> values = generateIntraDayData(ticker, startDate, days, 15)
		return new RequestValueSet(ticker, 0, values)
	}

	Collection<DataSetValue> generateIntraDayData(String ticker, Date startDate, int days, int intervalLength) {
		double currentPrice = ticker.size() * 10
		long seed = generateRandomSeed(ticker)
		SecureRandom random = new SecureRandom()
		random.setSeed(seed)
		return (0..(days - 1)).collectMany { int dayOffset ->
			Date currentDate = use (TimeCategory) { startDate + dayOffset.days }
			currentDate.set(hourOfDay: 9, minute: 30, second: 0, millisecond: 0)
			Collection<DataSetValue> values = []
			while (currentDate[Calendar.HOUR_OF_DAY] < 16 || (currentDate[Calendar.HOUR_OF_DAY] == 16 && currentDate[Calendar.MINUTE] == 0)) {
				values.push(new DataSetValue(currentDate, currentPrice))
				currentPrice = ((1 + (random.nextDouble() * 0.2 - 0.1)) * currentPrice).round(2)
				currentDate = use (TimeCategory) { currentDate + intervalLength.minutes }
			}
			return values
		}
	}

	long generateRandomSeed(String ticker) {
		String alphabet = ('A'..'Z').join('')
		String stringSeed = ticker.collect { String letter ->
			int index = alphabet.indexOf(letter.toUpperCase()) + 1
			return index <= 9 ? '0' + index : index
		}.join('')
		return stringSeed.toLong()
	}
}
