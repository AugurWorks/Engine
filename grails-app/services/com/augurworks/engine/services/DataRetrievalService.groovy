package com.augurworks.engine.services

import grails.transaction.Transactional
import groovyx.gpars.GParsPool

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.RequestValueSet

@Transactional
class DataRetrievalService {

	static final String QUANDL_DATE_FORMAT = 'yyyy-MM-dd'

	GrailsApplication grailsApplication

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
				return getSingleRequestValues(requestDataSet, algorithmRequest.startDate, algorithmRequest.endDate, minOffset, maxOffset)
			}
		}
	}

	RequestValueSet getSingleRequestValues(RequestDataSet requestDataSet, Date startDate, Date endDate, int minOffset, int maxOffset) {
		Collection<DataSetValue> values = getQuandlData(requestDataSet.dataSet.code, requestDataSet.dataSet.dataColumn)
		return new RequestValueSet(requestDataSet.dataSet.ticker, requestDataSet.offset, values).filterValues(startDate, endDate, minOffset, maxOffset)
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
}
