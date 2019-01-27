package com.augurworks.engine.services

import com.augurworks.engine.data.ActualValue
import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.instrumentation.Instrumentation
import com.augurworks.engine.model.DataSetValue
import com.augurworks.engine.model.RequestValueSet
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.timgroup.statsd.StatsDClient
import grails.converters.JSON
import grails.transaction.Transactional
import groovy.json.JsonBuilder
import org.apache.commons.lang.time.DateUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@Transactional
class ActualValueService {

	private final DateFormat DATE_FORMAT = new SimpleDateFormat('yyyy-MM-dd-HH-mm')

	private final Cache<String, DataSetValue> actualValueCache = CacheBuilder.newBuilder().expireAfterWrite(6, TimeUnit.HOURS).build()

	DataRetrievalService dataRetrievalService

	private static final Logger log = LoggerFactory.getLogger(ActualValueService)

	private final StatsDClient statsdClient = Instrumentation.statsdClient

	void fillOutPredictedValues() {
		Date yesterday = new Date(new Date().getTime() - 24 * 3600 * 1000)
		Date weekAgo = new Date(new Date().getTime() - 7 * 24 * 3600 * 1000)
		List<PredictedValue> predictedValues = PredictedValue.findAllByActualIsNullAndDateLessThanAndDateGreaterThan(yesterday, weekAgo)
		log.info('Filling out ' + predictedValues.size() + ' predicted values')
		statsdClient.recordGaugeValue('count.job.actual.required', predictedValues.size())
		predictedValues.each { PredictedValue predictedValue ->
			try {
				DataSetValue actualValue = getActualValue(predictedValue)
				if (actualValue) {
					predictedValue.actual = actualValue.value
					predictedValue.date = actualValue.date
					predictedValue.save()
					statsdClient.increment('count.job.actual.success')
				} else {
					log.warn('No actual value found for predicted value ' + predictedValue.id)
					statsdClient.increment('count.job.actual.empty')
				}
			} catch(Exception e) {
				log.error('Error getting actual value', e)
				statsdClient.increment('count.job.actual.error')
			}
		}
		log.info('Finished filling out predicted values')
	}

	private DataSetValue getActualValue(PredictedValue predictedValue) {
		RequestDataSet requestDataSet = predictedValue.algorithmResult.algorithmRequest.getDependentRequestDataSet()
		DataSetValue cachedValue = actualValueCache.getIfPresent(getCacheKey(predictedValue.date, requestDataSet.symbol))
		if (cachedValue != null) {
			statsdClient.increment('count.job.actual.cache.hit')
			return cachedValue
		}
		statsdClient.increment('count.job.actual.cache.miss')
		Date startDate = DateUtils.truncate(predictedValue.date, Calendar.DATE)
		Date endDate = startDate.next()
		SingleDataRequest dataRequest = new SingleDataRequest(
				symbolResult: requestDataSet.toSymbolResult(),
				offset: requestDataSet.offset,
				startDate: startDate,
				endDate: endDate,
				unit: predictedValue.algorithmResult.algorithmRequest.unit == Unit.DAY ? Unit.DAY : Unit.FIFTEEN_MINUTES,
				minOffset: 0,
				maxOffset: 0,
				aggregation: requestDataSet.aggregation,
				dataType: requestDataSet.dataType
		)
		RequestValueSet requestValueSet = new RequestValueSet(dataRequest.symbolResult.symbol, dataRequest.dataType, dataRequest.offset, dataRequest.getHistory()).aggregateValues(requestDataSet.aggregation)
		requestValueSet.getValues().each { dataSetValue ->
			actualValueCache.put(getCacheKey(dataSetValue.date, dataRequest.symbolResult.symbol), dataSetValue)
		}
		statsdClient.increment('count.job.actual.cache.put', requestValueSet.getValues().size())
		return requestValueSet.getValues().find { DataSetValue value ->
			return value.date == predictedValue.date
		}
	}

	Optional<ActualValue> getActual(AlgorithmResult algorithmResult) {
		if (!algorithmResult.futureValue) {
			return Optional.empty()
		}
		AlgorithmRequest algorithmRequest = algorithmResult.algorithmRequest
		RequestDataSet requestDataSet = algorithmRequest.dependentRequestDataSet
		SingleDataRequest singleDataRequest = new SingleDataRequest(
				symbolResult: requestDataSet.toSymbolResult(),
				offset: requestDataSet.offset,
				startDate: algorithmRequest.getStartDate(algorithmResult.dateCreated),
				endDate: algorithmRequest.getEndDate(algorithmResult.dateCreated),
				unit: algorithmRequest.unit,
				minOffset: requestDataSet.offset,
				maxOffset: requestDataSet.offset,
				aggregation: Aggregation.VALUE,
				dataType: requestDataSet.dataType
		)
		RequestValueSet predictionActuals = dataRetrievalService.getSingleRequestValues(singleDataRequest)
		int predictionOffset = algorithmRequest.predictionOffset - algorithmRequest.independentRequestDataSets*.offset.max()
		Date futureDate = algorithmRequest.unit.calculateOffset.apply(predictionActuals.values.last().date, predictionOffset)
		if (futureDate.getTime() == algorithmResult.futureValue?.date?.getTime()) {
			ActualValue actualValue = new ActualValue(
					predictedValue: requestDataSet.aggregation.normalize.apply(predictionActuals.values.last().value, algorithmResult.futureValue.value)?.round(3),
					currentValue: predictionActuals.values.last().value?.round(3),
					date: futureDate
			)
			return Optional.of(actualValue)
		}
		Collection<PredictedValue> predictedValues = algorithmResult.predictedValues
		log.warn('Prediction actual and predicted date arrays for ' + algorithmRequest + ' do not match up')
		log.info('- Last actual date: ' + predictionActuals.values.last().date)
		log.info('- Last prediction date: ' + algorithmResult.futureValue.date)
		log.debug('Prediction actuals: ' + new JsonBuilder(predictionActuals.values).toPrettyString())
		log.debug('Algorithm result prediction values : ' + (predictedValues as JSON))
		return Optional.empty()
	}

	private String getCacheKey(Date date, String symbol) {
		return DATE_FORMAT.format(date) + '-' + symbol
	}
}
