package com.augurworks.engine.services

import grails.transaction.Transactional
import groovy.time.TimeCategory

import java.security.SecureRandom

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.model.DataSetValue
import com.augurworks.engine.model.RequestValueSet

@Transactional
class DataGeneratorService {

	GrailsApplication grailsApplication

	static final Collection<Map> DEFAULT_REQUESTS = [[
		tickers: ['AAPL', 'BAC', 'GE', 'GOOG', 'GS', 'TSLA', 'JPM', 'ORCL', 'USO'],
		dependent: 'TSLA',
		startOffset: -15,
		endOffset: -1
	], [
		tickers: ['AAPL', 'BAC', 'GE', 'GOOG', 'GS', 'TSLA', 'JPM', 'ORCL', 'USO'],
		dependent: 'TSLA',
		startOffset: -22,
		endOffset: -1
	], [
		tickers: ['AAPL', 'BAC', 'GE', 'GOOG', 'GS', 'TSLA', 'JPM', 'ORCL', 'USO'],
		dependent: 'TSLA',
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

	void bootstrapDefaultRequests() {
		DEFAULT_REQUESTS.eachWithIndex { Map requestMap, int index ->
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(name: 'Request ' + index, startOffset: requestMap.startOffset, endOffset: requestMap.endOffset, dependantSymbol: requestMap.dependent + ' - CLOSE').save()
			requestMap.tickers.each { String ticker ->
				new RequestDataSet(
					symbol: ticker,
					name: ticker,
					datasource: Datasource.TD,
					offset: ticker == requestMap.dependent ? 0 : -1,
					aggregation: Aggregation.PERIOD_PERCENT_CHANGE,
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
