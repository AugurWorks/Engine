package com.augurworks.engine.services

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.ApiKey
import com.augurworks.engine.domains.Product
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue
import grails.core.GrailsApplication
import grails.transaction.Transactional
import groovy.time.TimeCategory

import java.security.SecureRandom

@Transactional
class DataGeneratorService {

	GrailsApplication grailsApplication

	static final Map DEFAULT_REQUEST = [
		tickers: ['AAPL', 'BAC', 'GE', 'GOOG', 'TSLA', 'JPM', 'ORCL', 'USO'],
		dependent: 'TSLA',
		startOffset: -15,
		endOffset: -1
	]

	void bootstrapDefaultRequests() {
		Product product = new Product(
				name: 'Test Product 1',
				volatilePercentLimit: 0.1,
				diffUpperThreshold: 10,
				diffLowerThreshold: 5,
				realTimeDiffThreshold: 1,
				realTimeChangeThreshold: -1,
				closeDiffThreshold: 1,
				closeChangeThreshold: -1)
		new ApiKey(name: 'AugurWorks', products: [product]).save()
		['Real Time', 'Close'].each { String name ->
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(
					name: 'Tesla - ' + name,
					startOffset: DEFAULT_REQUEST.startOffset,
					endOffset: DEFAULT_REQUEST.endOffset,
					dependantSymbol: DEFAULT_REQUEST.dependent + ' - CLOSE',
					product: product,
					unit: Unit.FIFTEEN_MINUTES
			).save()
			DEFAULT_REQUEST.tickers.each { String ticker ->
				new RequestDataSet(
					symbol: ticker,
					name: ticker,
					datasource: Datasource.TD,
					offset: ticker == DEFAULT_REQUEST.dependent ? 0 : -1,
					aggregation: Aggregation.PERIOD_PERCENT_CHANGE,
					algorithmRequest: algorithmRequest
				).save()
			}
		}
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
