package com.augurworks.engine.services

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.instrumentation.Instrumentation
import com.augurworks.engine.model.DataSetValue
import com.augurworks.engine.model.RequestValueSet
import com.augurworks.engine.rest.SymbolResult
import com.timgroup.statsd.StatsDClient
import grails.core.GrailsApplication
import grails.transaction.Transactional
import groovyx.gpars.GParsPool
import org.slf4j.MDC

@Transactional
class DataRetrievalService {

	private final StatsDClient statsdClient = Instrumentation.statsdClient

	GrailsApplication grailsApplication

	Collection<RequestValueSet> smartSpline(SplineRequest splineRequest) {
		Collection<RequestValueSet> rawRequestValues = getRequestValues(splineRequest)
		Collection<Date> allDates = splineRequest.algorithmRequest.splineType.reduceDates.apply(rawRequestValues*.dates)
		Collection<RequestValueSet> expandedRequestValues = rawRequestValues*.fillOutValues(allDates)
		if (splineRequest.prediction) {
			int predictionOffset = splineRequest.algorithmRequest.predictionOffset
			return expandedRequestValues*.reduceValueRange(splineRequest.startDate, splineRequest.algorithmRequest.unit.calculateOffset.apply(splineRequest.endDate, splineRequest.algorithmRequest.predictionOffset), predictionOffset)
		}
		return expandedRequestValues*.reduceValueRange(splineRequest.startDate, splineRequest.algorithmRequest.unit.calculateOffset.apply(splineRequest.endDate, splineRequest.algorithmRequest.predictionOffset))
	}

	Collection<RequestValueSet> getRequestValues(SplineRequest splineRequest) {
		int minOffset = splineRequest.algorithmRequest.requestDataSets*.offset.min() - 1
		int maxOffset = splineRequest.algorithmRequest.requestDataSets*.offset.max()
		Collection<RequestDataSet> requestDataSets = splineRequest.includeDependent ? splineRequest.algorithmRequest.requestDataSets : splineRequest.algorithmRequest.independentRequestDataSets
		long startTime = System.currentTimeMillis()
		try {
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
							aggregation: requestDataSet.aggregation,
							dataType: requestDataSet.dataType
					)
					return getSingleRequestValues(singleDataRequest)
				}
			}
		} finally {
			statsdClient.recordGaugeValue('histogram.data.request.total', System.currentTimeMillis() - startTime, 'un:ms')
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
		MDC.put('ticker', singleDataRequest.symbolResult.toString())
		long startTime = System.currentTimeMillis()
		try {
			Collection<DataSetValue> values = singleDataRequest.getHistory()
			statsdClient.recordGaugeValue('histogram.data.request.history', System.currentTimeMillis() - startTime, 'un:ms')
			return new RequestValueSet(singleDataRequest.symbolResult.symbol, singleDataRequest.dataType, singleDataRequest.offset, values).aggregateValues(singleDataRequest.aggregation)
		} catch (Exception e) {
			log.error('Unable to get history for ' + singleDataRequest.symbolResult.toString(), e)
			throw e
		} finally {
			statsdClient.recordGaugeValue('histogram.data.request.single', System.currentTimeMillis() - startTime, 'un:ms')
			MDC.remove('ticker')
		}
	}
}
