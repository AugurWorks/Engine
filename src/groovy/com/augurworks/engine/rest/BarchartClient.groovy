package com.augurworks.engine.rest

import grails.plugins.rest.client.RestBuilder

class BarchartClient {

	private final BARCHART_ROOT = 'http://ondemand.websol.barchart.com'
	private final HISTORY_LOOKUP = BARCHART_ROOT + '/getHistory.json'
	private final SYMBOL_LOOKUP = BARCHART_ROOT + '/getSymbolLookUp.json'

	private final API_KEY

	BarchartClient(String apiKey) {
		API_KEY = apiKey
	}

	Collection<Map> searchSymbol(String keyword) {
		return makeRequest(SYMBOL_LOOKUP, [keyword: keyword]).collect { Map result ->
			return [
				name: result.name,
				value: result.symbol + '-Barchart'
			]
		}
	}

	Collection<Map> getHistory(BarchartHistoryParameters parameters) {
		return makeRequest(HISTORY_LOOKUP, parameters.toParameters())
	}

	private Collection<Map> makeRequest(String url, Map parameters) {
		parameters.apikey = API_KEY
		String fullUrl = url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
		return new RestBuilder().get(fullUrl).json?.results ?: []
	}
}
