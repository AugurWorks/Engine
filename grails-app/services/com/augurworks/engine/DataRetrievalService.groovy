package com.augurworks.engine

import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.RequestValueSet
import grails.transaction.Transactional
import groovyx.gpars.GParsPool

@Transactional
class DataRetrievalService {

	def grailsApplication

	Collection<RequestValueSet> getRequestValues(AlgorithmRequest algorithmRequest) {
		int minOffset = algorithmRequest.requestDataSets*.offset.min()
		int maxOffset = algorithmRequest.requestDataSets*.offset.max()
		GParsPool.withPool(algorithmRequest.requestDataSets.size()) {
			return algorithmRequest.requestDataSets.collectParallel { RequestDataSet requestDataSet ->
				return new RequestValueSet(requestDataSet.dataSet.name, getDataSetValues(requestDataSet.dataSet, algorithmRequest.startDate, algorithmRequest.endDate, minOffset, maxOffset))
			}
		}
	}

	Collection<DataSetValue> getDataSetValues(DataSet dataSet, Date startDate, Date endDate, int minOffset, int maxOffset) {
		Collection<DataSetValue> rawData = getQuandlData(dataSet.code, dataSet.dataColumn)
		int startIndex = rawData.findIndexOf { it.date == startDate.format('yyyy-MM-dd') }
		int endIndex = rawData.findIndexOf { it.date == endDate.format('yyyy-MM-dd') }
		if (startIndex == -1 || endIndex == -1 || startIndex + minOffset < 0 || endIndex + maxOffset > rawData.size() - 1) {
			throw new AugurWorksException(dataSet.name + ' does not contain data for the requested range ')
		}
		return rawData[(startIndex + minOffset)..(endIndex + maxOffset)]
	}

	Collection<DataSetValue> getQuandlData(String quandlCode, int dataColumn) {
		String quandlKey = grailsApplication.config.augurworks.quandl.key
		String quandlPre = 'https://www.quandl.com/api/v1/datasets/'
		String quandlPost = '.csv?auth_token=' + quandlKey
		String url = quandlPre + quandlCode + quandlPost
		return new URL(url).getText().split('\n').tail().reverse().collect { String line ->
			Collection<String> lineValues = line.split(',')
			return new DataSetValue(lineValues[0], lineValues[dataColumn])
		}
	}
}
