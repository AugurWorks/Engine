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

	void "test hourly algorithm request retrieval"() {
		given:
			AlgorithmRequest hourRequest = createAlgorithmRequest(startOffset, endOffset, '.INX', 'Hour')
			AlgorithmRequest.metaClass.now = { return Date.parse('MM/dd/yyyy HH:mm', '02/12/2016 ' + timeString) }
			DataRetrievalService.metaClass.getGoogleAPIText = { String ticker, Date startDate, int intervalMinutes ->
				return new File('test/resources/hourly-data/Hourly-' + ticker + '.txt').text
			}

		when:
			Collection<RequestValueSet> requestValueSet = dataRetrievalService.smartSpline(hourRequest, true)

		then:
			notThrown AugurWorksException
			requestValueSet.size() == 6
			requestValueSet*.values*.size().max() == valuesSize

		where:
			startOffset | endOffset | timeString | valuesSize
			-4          | -1        | '14:30'    | 6
			-4          | -1        | '15:30'    | 5
			-6          | -4        | '16:30'    | 5
			-5          | -1        | '15:30'    | 7
	}

	void "test day algorithm request retrieval"() {
		given:
			AlgorithmRequest dayRequest = createAlgorithmRequest(startOffset, endOffset, '.INX', 'Day')
			AlgorithmRequest.metaClass.now = { return Date.parse('MM/dd/yyyy', dateString) }
			DataRetrievalService.metaClass.getQuandlAPIText = { String quandlCode ->
				return new File('test/resources/daily-data/' + quandlCode.split('/').join('-') + '.csv').text
			}

		when:
			Collection<RequestValueSet> requestValueSet = dataRetrievalService.smartSpline(dayRequest, true)

		then:
			notThrown AugurWorksException
			requestValueSet.size() == 6
			requestValueSet*.values*.size().max() == valuesSize

		where:
			startOffset | endOffset | dateString   | valuesSize
			-14         | -1        | '02/12/2016' | 11
			-8          | -1        | '02/12/2016' | 7
			-17         | -1        | '02/12/2016' | 14
			-11         | -8        | '02/12/2016' | 5
	}
}
