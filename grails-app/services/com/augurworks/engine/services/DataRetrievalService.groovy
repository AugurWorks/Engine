package com.augurworks.engine.services

import grails.plugin.cache.GrailsCacheManager
import grails.plugin.cache.GrailsValueWrapper
import grails.transaction.Transactional
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovyx.gpars.GParsPool

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.amazonaws.services.s3.AmazonS3Client
import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.helper.SplineRequest

@Transactional
class DataRetrievalService {

	static final String QUANDL_DATE_FORMAT = 'yyyy-MM-dd'
	static final String GOOGLE_API_ROOT = 'http://www.google.com/finance/getprices?'

	GrailsApplication grailsApplication
	DataGeneratorService dataGeneratorService
	GrailsCacheManager grailsCacheManager

	Collection<RequestValueSet> smartSpline(SplineRequest splineRequest) {
		Collection<RequestValueSet> rawRequestValues = getRequestValues(splineRequest)
		Collection<Date> allDates = rawRequestValues*.dates.flatten().unique()
		Collection<RequestValueSet> expandedRequestValues = rawRequestValues*.fillOutValues(allDates)
		if (splineRequest.prediction) {
			int predictionOffset = splineRequest.algorithmRequest.predictionOffset
			return expandedRequestValues*.reduceValueRange(splineRequest.startDate, splineRequest.endDate, predictionOffset)
		}
		return expandedRequestValues*.reduceValueRange(splineRequest.startDate, splineRequest.endDate)
	}

	Collection<RequestValueSet> getRequestValues(SplineRequest splineRequest) {
		int minOffset = splineRequest.algorithmRequest.requestDataSets*.offset.min()
		int maxOffset = splineRequest.algorithmRequest.requestDataSets*.offset.max()
		Collection<RequestDataSet> requestDataSets = splineRequest.includeDependent ? splineRequest.algorithmRequest.requestDataSets : splineRequest.algorithmRequest.independentRequestDataSets
		GParsPool.withPool(requestDataSets.size()) {
			return requestDataSets.collectParallel { RequestDataSet requestDataSet ->
				return getSingleRequestValues(requestDataSet, splineRequest.startDate, splineRequest.endDate, splineRequest.algorithmRequest.unit, minOffset, maxOffset)
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
				values = getGoogleData(requestDataSet.dataSet.ticker, startDate.clone(), 60)
				break
			case 'Half Hour':
				values = getGoogleData(requestDataSet.dataSet.ticker, startDate.clone(), 30)
				break
			default:
				throw new AugurWorksException('Unknown prediction date unit: ' + unit)
		}
		return new RequestValueSet(requestDataSet.dataSet.ticker, requestDataSet.offset, values).aggregateValues(requestDataSet.aggregation).filterValues(startDate, endDate, minOffset, maxOffset)
	}

	Collection<DataSetValue> getQuandlData(String quandlCode, int dataColumn) {
		return getQuandlAPIText(quandlCode).split('\n').tail().reverse().grep { String line ->
			Collection<String> lineValues = line.split(',')
			return lineValues[dataColumn].size() != 0
		}.collect { String line ->
			Collection<String> lineValues = line.split(',')
			return new DataSetValue(Date.parse(QUANDL_DATE_FORMAT, lineValues[0]), lineValues[dataColumn].toDouble())
		}
	}

	String getQuandlAPIText(String quandlCode) {
		String quandlKey = grailsApplication.config.augurworks.quandl.key
		String quandlPre = 'https://www.quandl.com/api/v1/datasets/'
		String quandlPost = '.csv?auth_token=' + quandlKey
		String url = quandlPre + quandlCode + quandlPost
		return getUrlText(url)
	}

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
			use (TimeCategory) {
				TimeDuration timeSinceStart = dataSetValue.date - startDate
				return timeSinceStart.minutes % intervalMinutes == 0
			}
		}
	}
	
	String getGoogleAPIText(String ticker, Date startDate, int intervalMinutes) {
		String text = getUrlText(constructGoogleUrl(ticker, startDate, intervalMinutes))
		if (grailsApplication.config.logging.files) {
			logStringToS3(ticker + '-Hourly', (['URL: ' + url.toString(), ''] + text.split('\n')).join('\n'))
		}
		return text
	}

	String getUrlText(String rawUrl) {
		GrailsValueWrapper cache = grailsCacheManager.getCache('externalData').get(rawUrl)
		if (cache) {
			return cache.get()
		}
		String text = new URL(rawUrl).getText()
		grailsCacheManager.getCache('externalData').put(rawUrl, text)
		return text
	}

	String constructGoogleUrl(String ticker, Date startDate, int intervalMinutes) {
		int period = use(TimeCategory) { (new Date() - startDate).days + 3 }
		return GOOGLE_API_ROOT + 'q=' + ticker + '&p=' + period + 'd&i=' + (intervalMinutes * 60) + '&f=d,c'
	}

	DataSetValue parseGoogleData(Date startDate, int intervalMinutes, String googleRow) {
		Collection<String> cols = googleRow.split(',')
		int offset = (cols[0].isInteger() ? cols[0].toInteger() : 0) * intervalMinutes
		Date date = use(TimeCategory) { startDate + offset.minutes }
		return new DataSetValue(date, cols[1].toDouble())
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
