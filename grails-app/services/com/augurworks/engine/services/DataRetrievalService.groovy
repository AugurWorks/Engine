package com.augurworks.engine.services

import grails.transaction.Transactional
import groovy.time.TimeCategory
import groovyx.gpars.GParsPool

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.RequestValueSet

@Transactional
class DataRetrievalService {

	static final String QUANDL_DATE_FORMAT = 'yyyy-MM-dd'
	static final String GOOGLE_API_ROOT = 'http://www.google.com/finance/getprices?'

	GrailsApplication grailsApplication
	DataGeneratorService dataGeneratorService

	Collection<RequestValueSet> smartSpline(AlgorithmRequest algorithmRequest, boolean prediction) {
		Collection<RequestValueSet> rawRequestValues = getRequestValues(algorithmRequest, prediction)
		Collection<Date> allDates = rawRequestValues*.dates.flatten().unique()
		Collection<RequestValueSet> expandedRequestValues = rawRequestValues*.fillOutValues(allDates)
		if (prediction) {
			int predictionOffset = algorithmRequest.predictionOffset
			return expandedRequestValues*.reduceValueRange(algorithmRequest.startDate, algorithmRequest.endDate, predictionOffset)
		}
		return expandedRequestValues*.reduceValueRange(algorithmRequest.startDate, algorithmRequest.endDate)
	}

	Collection<RequestValueSet> getRequestValues(AlgorithmRequest algorithmRequest, boolean prediction) {
		int minOffset = algorithmRequest.requestDataSets*.offset.min()
		int maxOffset = algorithmRequest.requestDataSets*.offset.max()
		Collection<RequestDataSet> requestDataSets = prediction ? algorithmRequest.independentRequestDataSets : algorithmRequest.requestDataSets
		GParsPool.withPool(requestDataSets.size()) {
			return requestDataSets.collectParallel { RequestDataSet requestDataSet ->
				return getSingleRequestValues(requestDataSet, algorithmRequest.startDate, algorithmRequest.endDate, algorithmRequest.unit, minOffset, maxOffset)
			}
		}
	}

	RequestValueSet getSingleRequestValues(RequestDataSet requestDataSet, Date startDate, Date endDate, String unit, int minOffset, int maxOffset) {
		Collection<DataSetValue> values = []
		switch (unit) {
			case 'Day':
				values = getQuandlData(requestDataSet.dataSet.code, requestDataSet.dataSet.dataColumn)
				break
			case 'Hour':
				Date minStartDate = use(TimeCategory) { startDate - 1.days }
				int dayCount = use(TimeCategory) { (endDate - minStartDate).days } + 1
				values = dataGeneratorService.generateIntraDayData(requestDataSet.dataSet.ticker, minStartDate, dayCount, 30)
				break
		}
		return new RequestValueSet(requestDataSet.dataSet.ticker, requestDataSet.offset, values).aggregateValues(requestDataSet.aggregation).filterValues(startDate, endDate, minOffset, maxOffset)
	}

	Collection<DataSetValue> getQuandlData(String quandlCode, int dataColumn) {
		String quandlKey = grailsApplication.config.augurworks.quandl.key
		String quandlPre = 'https://www.quandl.com/api/v1/datasets/'
		String quandlPost = '.csv?auth_token=' + quandlKey
		String url = quandlPre + quandlCode + quandlPost
		return new URL(url).getText().split('\n').tail().reverse().grep { String line ->
			Collection<String> lineValues = line.split(',')
			return lineValues[dataColumn].size() != 0
		}.collect { String line ->
			Collection<String> lineValues = line.split(',')
			return new DataSetValue(Date.parse(QUANDL_DATE_FORMAT, lineValues[0]), lineValues[dataColumn].toDouble())
		}
	}


	Collection<DataSetValue> getGoogleData(String ticker, Date startDate, int intervalMinutes) {
		URL url = new URL(constructGoogleUrl(ticker, startDate, intervalMinutes))
		Collection<String> vals = url.getText().split('\n')
		int openMinute = vals[1].split('=')[1].toInteger()
		startDate.set(minute: openMinute)
		int interval = vals[3].split('=')[1].toInteger() / intervalMinutes
		Collection<String> data = vals[7..(vals.size() - 1)]
		Date actualStart = new Date((data[0].split(',')[0] - 'a').toLong() * 1000)
		return data.collect { String rawString ->
			return parseGoogleData(actualStart, intervalMinutes, rawString)
		}
	}

	String constructGoogleUrl(String ticker, Date startDate, int intervalMinutes) {
		int period = use(TimeCategory) { (new Date() - startDate).days + 1 }
		return GOOGLE_API_ROOT + 'q=' + ticker + '&p=' + period + 'd&i=' + (intervalMinutes * 60) + '&f=d,c'
	}

	DataSetValue parseGoogleData(Date startDate, int intervalMinutes, String googleRow) {
		Collection<String> cols = googleRow.split(',')
		int offset = (cols[0].isInteger() ? cols[0].toInteger() : 0) * intervalMinutes
		Date date = use(TimeCategory) { startDate + offset.minutes }
		return new DataSetValue(date, cols[1].toDouble())
	}
}
