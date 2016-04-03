package com.augurworks.engine.services

import grails.plugin.cache.GrailsCacheManager
import grails.plugin.cache.GrailsValueWrapper
import grails.transaction.Transactional
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovyx.gpars.GParsPool

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.amazonaws.services.s3.AmazonS3Client
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.helper.SingleDataRequest
import com.augurworks.engine.helper.SplineRequest
import com.augurworks.engine.rest.SymbolResult

@Transactional
class DataRetrievalService {

	static final String QUANDL_DATE_FORMAT = 'yyyy-MM-dd'
	static final String GOOGLE_API_ROOT = 'http://www.google.com/finance/getprices?'

	@SuppressWarnings("GrailsStatelessService")
	GrailsCacheManager grailsCacheManager
	GrailsApplication grailsApplication
	DataGeneratorService dataGeneratorService

	Collection<RequestValueSet> smartSpline(SplineRequest splineRequest) {
		Collection<RequestValueSet> rawRequestValues = getRequestValues(splineRequest)
		Collection<Date> allDates = rawRequestValues*.dates.flatten().unique()
		Collection<RequestValueSet> expandedRequestValues = rawRequestValues*.fillOutValues(allDates)
		if (splineRequest.prediction) {
			int predictionOffset = splineRequest.algorithmRequest.predictionOffset
			return expandedRequestValues*.reduceValueRange(splineRequest.algorithmRequest.unit, splineRequest.startDate, splineRequest.endDate, predictionOffset)
		}
		return expandedRequestValues*.reduceValueRange(splineRequest.algorithmRequest.unit, splineRequest.startDate, splineRequest.endDate)
	}

	Collection<RequestValueSet> getRequestValues(SplineRequest splineRequest) {
		int minOffset = splineRequest.algorithmRequest.requestDataSets*.offset.min()
		int maxOffset = splineRequest.algorithmRequest.requestDataSets*.offset.max()
		Collection<RequestDataSet> requestDataSets = splineRequest.includeDependent ? splineRequest.algorithmRequest.requestDataSets : splineRequest.algorithmRequest.independentRequestDataSets
		GParsPool.withPool(requestDataSets.size()) {
			return requestDataSets.collectParallel { RequestDataSet requestDataSet ->
				SingleDataRequest singleDataRequest = new SingleDataRequest(
					symbolResult: requestDataSet.toSymbolResult(),
					offset: requestDataSet.offset,
					startDate: splineRequest.startDate,
					endDate: splineRequest.endDate,
					unit: splineRequest.algorithmRequest.unit,
					minOffset: minOffset,
					maxOffset: maxOffset,
					aggregation: requestDataSet.aggregation
				)
				return getSingleRequestValues(singleDataRequest)
			}
		}
	}

	RequestValueSet getSingleRequestValues(SingleDataRequest singleDataRequest) {
		Collection<DataSetValue> values = singleDataRequest.getHistory()
		return new RequestValueSet(singleDataRequest.symbolResult.symbol, singleDataRequest.offset, values).aggregateValues(singleDataRequest.aggregation).filterValues(singleDataRequest.unit, singleDataRequest.startDate, singleDataRequest.endDate, singleDataRequest.minOffset, singleDataRequest.maxOffset)
	}

	@Deprecated
	Collection<DataSetValue> getQuandlData(String quandlCode, int dataColumn) {
		return getQuandlAPIText(quandlCode).split('\n').tail().reverse().grep { String line ->
			Collection<String> lineValues = line.split(',')
			return lineValues[dataColumn].size() != 0
		}.collect { String line ->
			Collection<String> lineValues = line.split(',')
			return new DataSetValue(Date.parse(QUANDL_DATE_FORMAT, lineValues[0]), lineValues[dataColumn].toDouble())
		}
	}

	@Deprecated
	String getQuandlAPIText(String quandlCode) {
		String quandlKey = grailsApplication.config.augurworks.quandl.key
		String quandlPre = 'https://www.quandl.com/api/v1/datasets/'
		String quandlPost = '.csv?auth_token=' + quandlKey
		String url = quandlPre + quandlCode + quandlPost
		return getUrlText(url)
	}

	@Deprecated
	Collection<DataSetValue> getGoogleData(String ticker, Date startDate, int intervalMinutes) {
		int apiIntervalMinutes = Math.min(intervalMinutes, 30)
		Collection<String> vals = getGoogleAPIText(ticker, startDate, apiIntervalMinutes).split('\n')
		if (vals.size() == 6) {
			throw new AugurWorksException('No intra-day data available for ' + ticker)
		}
		Collection<String> data = vals[7..(vals.size() - 1)]
		Date actualStart = new Date((data[0].split(',')[0] - 'a').toLong() * 1000)
		return data.collect { String rawString ->
			return parseGoogleData(actualStart, apiIntervalMinutes, rawString)
		}.grep { DataSetValue dataSetValue ->
			if (!dataSetValue) {
				return false
			}
			use (TimeCategory) {
				TimeDuration timeSinceStart = dataSetValue.date - startDate
				return timeSinceStart.minutes % intervalMinutes == 0
			}
		}
	}

	@Deprecated
	String getGoogleAPIText(String ticker, Date startDate, int intervalMinutes) {
		String url = constructGoogleUrl(ticker, startDate, intervalMinutes)
		String text = getUrlText(url)
		if (grailsApplication.config.logging.files) {
			logStringToS3(ticker + '-Hourly', (['URL: ' + url, ''] + text.split('\n')).join('\n'))
		}
		return text
	}

	@Deprecated
	String getUrlText(String rawUrl) {
		GrailsValueWrapper cache = grailsCacheManager.getCache('externalData').get(rawUrl)
		if (cache) {
			return cache.get()
		}
		String text = new URL(rawUrl).getText()
		grailsCacheManager.getCache('externalData').put(rawUrl, text)
		return text
	}

	@Deprecated
	String constructGoogleUrl(String ticker, Date startDate, int intervalMinutes) {
		int period = use(TimeCategory) { (new Date() - startDate).days + 3 }
		return GOOGLE_API_ROOT + 'q=' + ticker + '&p=' + period + 'd&i=' + (intervalMinutes * 60) + '&f=d,c'
	}

	@Deprecated
	DataSetValue parseGoogleData(Date startDate, int intervalMinutes, String googleRow) {
		if (googleRow.indexOf('TIMEZONE_OFFSET') != -1) {
			return null
		}
		Collection<String> cols = googleRow.split(',')
		int offset = (cols[0].isInteger() ? cols[0].toInteger() : 0) * intervalMinutes
		Date date = use(TimeCategory) { startDate + offset.minutes }
		return new DataSetValue(date, cols[1].toDouble())
	}

	@Deprecated
	Collection<SymbolResult> searchSymbol(String keyword) {
		GParsPool.withPool(Datasource.values().size()) {
			return Datasource.values().collectParallel { Datasource datasource ->
				return datasource.apiClient.searchSymbol(keyword)
			}.flatten()
		}
	}

	void logStringToS3(String filename, String text) {
		File file = File.createTempFile(filename, '.txt')
		file.write(text)
		AmazonS3Client s3 = new AmazonS3Client()
		String bucket = grailsApplication.config.aws.bucket
		Date date = new Date()
		String path = 'logs/' + date.format(Global.S3_DATE_FORMAT) + filename + '.txt'
		s3.putObject(bucket, path, file)
		file.delete()
	}
}
