package com.augurworks.engine.services

import grails.plugin.cache.GrailsCacheManager
import grails.transaction.Transactional
import groovyx.gpars.GParsPool

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.RequestValueSet
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
		Collection<Date> allDates = rawRequestValues*.dates.flatten().sort().unique()
		Collection<RequestValueSet> expandedRequestValues = rawRequestValues*.fillOutValues(allDates)
		if (splineRequest.prediction) {
			int predictionOffset = splineRequest.algorithmRequest.predictionOffset
			return expandedRequestValues*.reduceValueRange(splineRequest.algorithmRequest.unit, splineRequest.startDate, splineRequest.endDate, predictionOffset)
		}
		return expandedRequestValues*.reduceValueRange(splineRequest.algorithmRequest.unit, splineRequest.startDate, splineRequest.endDate)
	}

	Collection<RequestValueSet> getRequestValues(SplineRequest splineRequest) {
		int minOffset = splineRequest.algorithmRequest.requestDataSets*.offset.min() - 1
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

	Collection<SymbolResult> searchSymbol(String keyword) {
		GParsPool.withPool(Datasource.values().size()) {
			return Datasource.values().collectParallel { Datasource datasource ->
				return datasource.apiClient.searchSymbol(keyword)
			}.flatten()
		}
	}

	RequestValueSet getSingleRequestValues(SingleDataRequest singleDataRequest) {
		Collection<DataSetValue> values = singleDataRequest.getHistory()
		return new RequestValueSet(singleDataRequest.symbolResult.symbol, singleDataRequest.offset, values).aggregateValues(singleDataRequest.aggregation).filterValues(singleDataRequest.unit, singleDataRequest.startDate, singleDataRequest.endDate, singleDataRequest.minOffset, singleDataRequest.maxOffset)
	}
}
