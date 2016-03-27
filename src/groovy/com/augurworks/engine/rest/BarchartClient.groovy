package com.augurworks.engine.rest

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders

import org.joda.time.DateTime

import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Datasource

class BarchartClient implements ApiClient {

	static final String DATE_FORMAT = 'yyyyMMddHHmm'

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
		return makeRequest(HISTORY_LOOKUP, historyParametersToMap(parameters)).collect { Map result ->
			return new DataSetValue(new DateTime(result.timestamp).toDate(), result.close)
		}
	}

	private Map historyParametersToMap(HistoryParameters historyParameters) {
		Map parameters = [
			symbol: historyParameters.symbolResult.symbol,
			type: historyParameters.type == 'Day' ? 'daily' : 'minutes',
			startDate: historyParameters.startDate.format(DATE_FORMAT),
			endDate: historyParameters.endDate.format(DATE_FORMAT)
		]
		if (historyParameters.interval) {
			parameters.interval = historyParameters.interval.toString()
		}
		return parameters
	}

	private Collection<Map> makeRequest(String url, Map parameters) {
		parameters.apikey = API_KEY
		String fullUrl = url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
		return new RestBuilder().get(fullUrl).json?.results ?: []
	}
}
