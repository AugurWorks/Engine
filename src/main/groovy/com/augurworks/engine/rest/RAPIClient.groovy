package com.augurworks.engine.rest

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue
import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import grails.util.Holders
import groovy.json.JsonBuilder
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateUtils

class RAPIClient extends RestClient {

	private final location

    RAPIClient() {
		location = Holders.config.augurworks.rapi.location
	}

	Collection<SymbolResult> searchSymbol(String keyword) {
		return []
	}

	Collection<DataSetValue> getHistory(SingleDataRequest dataRequest) {
		Collection<Map> results = makeRequest(historyParametersToMap(dataRequest))
		logStringToS3(dataRequest.symbolResult.datasource.name() + '-' + dataRequest.symbolResult.symbol, new JsonBuilder(results).toPrettyString())
		return results.collect { Map result ->
			Date date = new Date(result.date.toLong() * 1000)
			if (dataRequest.unit == Unit.DAY) {
				date = DateUtils.truncate(date, Calendar.DATE)
			}
			return new DataSetValue(date, Double.parseDouble(result.val))
		}
	}

	private Map historyParametersToMap(SingleDataRequest dataRequest) {
		return [
				exchange: dataRequest.symbolResult.symbol.split('/').first(),
				ticker: dataRequest.symbolResult.symbol.split('/').last(),
				type: dataRequest.unit == Unit.DAY ? 'DAY' : 'MINUTE',
				start: (dataRequest.unit.calculateOffset.apply(dataRequest.startDate, -5).getTime() / 1000).toString(),
				end: (dataRequest.unit.calculateOffset.apply(dataRequest.endDate, 3).getTime() / 1000).toString(),
				period: (dataRequest.unit == Unit.DAY ? 24 * 60 : 15).toString()
		]
	}

	private Collection<Map> makeRequest(Map parameters) {
		String fullUrl = location + '?' + parameters.keySet().grep { String key -> StringUtils.isNotBlank(parameters[key]) }.collect { String key ->
			return key + '=' + URLEncoder.encode(parameters[key])
		}.join('&')
		return JSON.parse(new RestBuilder().get(fullUrl).text) ?: []
	}
}
