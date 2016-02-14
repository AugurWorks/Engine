package com.augurworks.engine

import grails.test.spock.IntegrationSpec

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.services.DataRetrievalService

class AlgorithmRequestIntegrationSpec extends IntegrationSpec {

	DataRetrievalService dataRetrievalService

	AlgorithmRequest createAlgorithmRequest(int startOffset, int endOffset, String dependantDataSetTicker, String unit) {
		AlgorithmRequest algorithmRequest = new AlgorithmRequest(
			startOffset: startOffset,
			endOffset: endOffset,
			dateCreated: new Date(),
			dependantDataSet: DataSet.findByTicker(dependantDataSetTicker),
			unit: unit
		)
		algorithmRequest.save()
		['.INX', 'AMZN', 'BAC', 'GOOG', 'JPM', 'USO'].each { String ticker ->
			algorithmRequest.addToRequestDataSets(
				dataSet: DataSet.findByTicker(ticker),
				offset: ticker == dependantDataSetTicker ? 0 : -1,
				aggregation: 'Period Percent Change'
			)
		}
		algorithmRequest.save(flush: true)
		return algorithmRequest
	}
}
