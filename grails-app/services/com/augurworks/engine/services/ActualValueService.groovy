package com.augurworks.engine.services

import com.augurworks.engine.model.RequestValueSet
import grails.transaction.Transactional

import org.apache.commons.lang.time.DateUtils

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Transactional
class ActualValueService {

	private static final Logger log = LoggerFactory.getLogger(ActualValueService)

	void fillOutPredictedValues() {
		Date yesterday = new Date(new Date().getTime() - 24 * 3600 * 1000)
		List<PredictedValue> predictedValues = PredictedValue.findAllByActualIsNullAndDateLessThan(yesterday)
		log.info('Filling out ' + predictedValues.size() + ' predicted values')
		predictedValues.each { PredictedValue predictedValue ->
			try {
				RequestDataSet requestDataSet = predictedValue.algorithmResult.algorithmRequest.getDependentRequestDataSet()
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
				DataSetValue actualValue = requestValueSet.getValues().find { DataSetValue value ->
					return value.date == predictedValue.date
				}
				if (actualValue) {
					predictedValue.actual = actualValue.value
					predictedValue.save()
				} else {
					log.warn('No actual value found for predicted value ' + predictedValue.id)
				}
			} catch(Exception e) {
				log.error('Error getting actual value', e)
			}
		}
		log.info('Finished filling out predicted values')
	}
}
