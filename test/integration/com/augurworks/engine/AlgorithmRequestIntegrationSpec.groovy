package com.augurworks.engine

import grails.test.spock.IntegrationSpec

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.helper.SplineRequest
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
				aggregation: Aggregation.PERIOD_PERCENT_CHANGE
			)
		}
		algorithmRequest.save(flush: true)
		return algorithmRequest
	}

	void "test hourly algorithm request retrieval"() {
		given:
			AlgorithmRequest hourRequest = createAlgorithmRequest(startOffset, endOffset, '.INX', 'Hour')
			DataRetrievalService.metaClass.getGoogleAPIText = { String ticker, Date startDate, int intervalMinutes ->
				return new File('test/resources/hourly-data/Hourly-' + ticker + '.txt').text
			}

		when:
			SplineRequest splineRequest = new SplineRequest(algorithmRequest: hourRequest, prediction: prediction, now: Date.parse('MM/dd/yyyy HH:mm', '02/12/2016 ' + timeString))
			Collection<RequestValueSet> requestValueSet = dataRetrievalService.smartSpline(splineRequest)

		then:
			notThrown AugurWorksException
			requestValueSet.size() == 6
			requestValueSet*.values*.size().max() == maxValuesSize
			requestValueSet*.values*.size().min() == minValuesSize

		where:
			startOffset | endOffset | timeString | prediction | maxValuesSize | minValuesSize
			-4          | -1        | '14:30'    | false      | 4             | 4
			-4          | -1        | '15:30'    | false      | 4             | 4
			-6          | -4        | '16:30'    | false      | 3             | 3
			-5          | -1        | '15:30'    | false      | 5             | 5
			-4          | -1        | '14:30'    | true       | 5             | 4
			-4          | -1        | '15:30'    | true       | 5             | 4
			-6          | -4        | '16:30'    | true       | 4             | 3
			-5          | -1        | '15:30'    | true       | 6             | 5
	}

	void "test day algorithm request retrieval"() {
		given:
			AlgorithmRequest dayRequest = createAlgorithmRequest(startOffset, endOffset, '.INX', 'Day')
			DataRetrievalService.metaClass.getQuandlAPIText = { String quandlCode ->
				return new File('test/resources/daily-data/' + quandlCode.split('/').join('-') + '.csv').text
			}

		when:
			SplineRequest splineRequest = new SplineRequest(algorithmRequest: dayRequest, prediction: prediction, now: Date.parse('MM/dd/yyyy', dateString))
			Collection<RequestValueSet> requestValueSet = dataRetrievalService.smartSpline(splineRequest)

		then:
			notThrown AugurWorksException
			requestValueSet.size() == 6
			requestValueSet*.values*.size().max() == maxValuesSize
			requestValueSet*.values*.size().min() == minValuesSize

		where:
			startOffset | endOffset | dateString   | prediction | maxValuesSize | minValuesSize
			-14         | -1        | '02/12/2016' | false      | 10            | 10
			-8          | -1        | '02/12/2016' | false      | 6             | 6
			-17         | -1        | '02/12/2016' | false      | 13            | 13
			-11         | -8        | '02/12/2016' | false      | 4             | 4
			-14         | -1        | '02/12/2016' | true       | 11            | 10
			-8          | -1        | '02/12/2016' | true       | 7             | 6
			-17         | -1        | '02/12/2016' | true       | 14            | 13
			-11         | -8        | '02/12/2016' | true       | 5             | 4
	}
}
