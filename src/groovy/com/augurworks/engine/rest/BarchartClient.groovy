package com.augurworks.engine.rest

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders

import org.joda.time.DateTime

import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Datasource

class BarchartClient implements ApiClient {

	private final String dateFormat = 'yyyyMMddHHmm'

	private final barchartRoot = 'http://ondemand.websol.barchart.com'
	private final historyLookup = barchartRoot + '/getHistory.json'
	private final symbolLookup = barchartRoot + '/getSymbolLookUp.json'

	private final apiKey

	BarchartClient() {
		apiKey = Holders.config.augurworks.barchart.key
	}

	Collection<SymbolResult> searchSymbol(String keyword) {
		return makeRequest(symbolLookup, [keyword: keyword]).collect { Map result ->
			return new SymbolResult(name: result.name, symbol: result.symbol, datasource: Datasource.BARCHART)
		}
	}

	Collection<DataSetValue> getHistory(HistoryParameters parameters) {
		return makeRequest(historyLookup, historyParametersToMap(parameters)).collect { Map result ->
			return new DataSetValue(new DateTime(result.timestamp).toDate(), result.close)
		}
	}

	private Map historyParametersToMap(HistoryParameters historyParameters) {
		Map parameters = [
			symbol: historyParameters.symbolResult.symbol,
			type: historyParameters.type == 'Day' ? 'daily' : 'minutes',
			startDate: historyParameters.startDate.format(dateFormat),
			endDate: historyParameters.endDate.format(dateFormat)
		]
		if (historyParameters.interval) {
			parameters.interval = historyParameters.interval.toString()
		}
		return parameters
	}

	private Collection<Map> makeRequest(String url, Map parameters) {
		parameters.apikey = apiKey
		String fullUrl = url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
		return new RestBuilder().get(fullUrl).json?.results ?: []
	}
}
