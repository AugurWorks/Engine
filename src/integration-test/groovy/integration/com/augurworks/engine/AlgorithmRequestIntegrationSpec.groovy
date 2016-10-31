package com.augurworks.engine

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.RequestValueSet
import com.augurworks.engine.rest.BarchartClient
import com.augurworks.engine.services.DataRetrievalService
import grails.converters.JSON
import grails.test.mixin.integration.Integration
import org.springframework.test.annotation.Rollback
import spock.lang.Specification

@Integration
@Rollback
class AlgorithmRequestIntegrationSpec extends Specification {

	DataRetrievalService dataRetrievalService

	private AlgorithmRequest createAlgorithmRequest(Collection<String> symbols, int startOffset, int endOffset, Datasource datasource, Unit unit) {
		String dependantDataSetTicker = symbols.first()
		AlgorithmRequest algorithmRequest = new AlgorithmRequest(
			startOffset: startOffset,
			endOffset: endOffset,
			dateCreated: new Date(),
			dependantSymbol: dependantDataSetTicker,
			unit: unit
		)
		symbols.each { String ticker ->
			algorithmRequest.addToRequestDataSets(
				symbol: ticker,
				name: ticker,
				datasource: datasource,
				offset: ticker == dependantDataSetTicker ? 0 : -1,
				aggregation: Aggregation.PERIOD_PERCENT_CHANGE
			)
		}
		return algorithmRequest
	}

	void "test barchart algorithm request retrieval"() {
		given:
			AlgorithmRequest algorithmRequest = createAlgorithmRequest(['SPM16', 'DLJ16'], startOffset, endOffset, Datasource.BARCHART, unit)
			BarchartClient.metaClass.makeRequest { SingleDataRequest singleDataRequest ->
				return JSON.parse(new File('src/test/resources/barchart/' + (singleDataRequest.unit == Unit.DAY ? 'day' : 'half-hour') + '/' + singleDataRequest.symbolResult.symbol + '.json').text).results
			}

		when:
			SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest, prediction: prediction, now: Date.parse('MM/dd/yyyy HH:mm', timeString))
			Collection<RequestValueSet> requestValueSet = dataRetrievalService.smartSpline(splineRequest)

		then:
			notThrown AugurWorksException
			requestValueSet.size() == 2
			requestValueSet*.values*.size().max() == maxValuesSize
			requestValueSet*.values*.size().min() == minValuesSize

		where:
			unit           | startOffset | endOffset | timeString         | prediction | maxValuesSize | minValuesSize
			Unit.HOUR      | -4          | -1        | '03/31/2016 14:30' | false      | 4             | 4
			Unit.HOUR      | -4          | -1        | '03/31/2016 15:30' | false      | 4             | 4
			Unit.HOUR      | -6          | -4        | '03/31/2016 16:30' | false      | 3             | 3
			Unit.HOUR      | -5          | -1        | '03/31/2016 15:30' | false      | 5             | 5
			Unit.HOUR      | -4          | -1        | '03/31/2016 14:30' | true       | 5             | 4
			Unit.HOUR      | -4          | -1        | '03/31/2016 15:30' | true       | 5             | 4
			Unit.HOUR      | -6          | -4        | '03/31/2016 16:30' | true       | 4             | 3
			Unit.HOUR      | -5          | -1        | '03/31/2016 15:30' | true       | 6             | 5
			Unit.HALF_HOUR | -4          | -1        | '03/31/2016 14:30' | false      | 4             | 4
			Unit.HALF_HOUR | -4          | -1        | '03/31/2016 15:30' | false      | 4             | 4
			Unit.HALF_HOUR | -6          | -4        | '03/31/2016 16:30' | false      | 3             | 3
			Unit.HALF_HOUR | -5          | -1        | '03/31/2016 15:30' | false      | 5             | 5
			Unit.HALF_HOUR | -4          | -1        | '03/31/2016 14:30' | true       | 5             | 4
			Unit.HALF_HOUR | -4          | -1        | '03/31/2016 15:30' | true       | 5             | 4
			Unit.HALF_HOUR | -6          | -4        | '03/31/2016 16:30' | true       | 4             | 3
			Unit.HALF_HOUR | -5          | -1        | '03/31/2016 15:30' | true       | 6             | 5
			Unit.DAY       | -7          | -1        | '03/31/2016 00:00' | false      | 4             | 4
			Unit.DAY       | -7          | -1        | '03/31/2016 00:00' | false      | 4             | 4
			Unit.DAY       | -8          | -3        | '03/31/2016 00:00' | false      | 3             | 3
			Unit.DAY       | -8          | -1        | '03/31/2016 00:00' | false      | 5             | 5
			Unit.DAY       | -7          | -1        | '03/31/2016 00:00' | true       | 5             | 4
			Unit.DAY       | -7          | -2        | '03/31/2016 00:00' | true       | 4             | 3
			Unit.DAY       | -9          | -3        | '03/31/2016 00:00' | true       | 5             | 4
			Unit.DAY       | -8          | -1        | '03/31/2016 00:00' | true       | 6             | 5
	}
}
