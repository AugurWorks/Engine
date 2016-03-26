package com.augurworks.engine.rest

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders
import groovy.util.slurpersupport.GPathResult

import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Datasource

class TDClient implements ApiClient {

	private final TD_ROOT = 'https://apis.tdameritrade.com/apps/100/'
	private final HISTORY_LOOKUP = TD_ROOT + '/PriceHistory'
	private final SYMBOL_LOOKUP = TD_ROOT + '/SymbolLookup'

	private final SOURCE_ID

	TDClient() {
		SOURCE_ID = Holders.config.augurworks.td.key
	}

	Collection<SymbolResult> searchSymbol(String keyword) {
		Collection<GPathResult> xmlResults = makeRequest(SYMBOL_LOOKUP, [matchstring: keyword]).depthFirst().findAll { GPathResult result ->
			return result.name() == 'symbol-result'
		}
		return xmlResults.collect { GPathResult result ->
			return new SymbolResult(name: result.getProperty('description'), symbol: result.getProperty('symbol'), datasource: Datasource.TD)
		}
	}

	Collection<DataSetValue> getHistory(BarchartHistoryParameters parameters) {
		return []
	}

	private GPathResult makeRequest(String url, Map parameters) {
		parameters.source = SOURCE_ID
		String fullUrl = url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
		return new RestBuilder().get(fullUrl).xml
	}
}
