package com.augurworks.engine.rest

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders

import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Datasource

class BarchartClient implements ApiClient {

	private final BARCHART_ROOT = 'http://ondemand.websol.barchart.com'
	private final HISTORY_LOOKUP = BARCHART_ROOT + '/getHistory.json'
	private final SYMBOL_LOOKUP = BARCHART_ROOT + '/getSymbolLookUp.json'

	private final API_KEY

	BarchartClient() {
		API_KEY = Holders.config.augurworks.barchart.key
	}

	Collection<SymbolResult> searchSymbol(String keyword) {
		return makeRequest(SYMBOL_LOOKUP, [keyword: keyword]).collect { Map result ->
			return new SymbolResult(name: result.name, symbol: result.symbol, datasource: Datasource.BARCHART)
		}
	}

	Collection<DataSetValue> getHistory(HistoryParameters parameters) {
		return makeRequest(HISTORY_LOOKUP, parameters.toParameters()).collect { Map result ->
			return new DataSetValue(date: new Date(result.timestamp), value: result.close)
		}
	}

	private Collection<Map> makeRequest(String url, Map parameters) {
		parameters.apikey = API_KEY
		String fullUrl = url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
		return new RestBuilder().get(fullUrl).json?.results ?: []
	}
}
