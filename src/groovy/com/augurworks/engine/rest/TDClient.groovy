package com.augurworks.engine.rest

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders
import groovy.util.slurpersupport.GPathResult

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Datasource

class TDClient implements ApiClient {

	static final String DATE_FORMAT = 'yyyyMMdd'

	private final TD_ROOT = 'https://apis.tdameritrade.com/apps/100'
	private final HISTORY_LOOKUP = TD_ROOT + '/PriceHistory'
	private final SYMBOL_LOOKUP = TD_ROOT + '/SymbolLookup'

	private final SOURCE_ID

	TDClient() {
		SOURCE_ID = Holders.config.augurworks.td.key
	}

	Collection<SymbolResult> searchSymbol(String keyword) {
		Collection<GPathResult> xmlResults = makeXmlRequest(SYMBOL_LOOKUP, [matchstring: keyword]).depthFirst().findAll { GPathResult result ->
			return result.name() == 'symbol-result'
		}
		return xmlResults.collect { GPathResult result ->
			return new SymbolResult(name: result.getProperty('description'), symbol: result.getProperty('symbol'), datasource: Datasource.TD)
		}
	}

	Collection<DataSetValue> getHistory(HistoryParameters parameters) {
		DataInputStream binaryResults = makeBinaryRequest(HISTORY_LOOKUP, historyParametersToMap(parameters))
		Collection<Map> parsedResults = parseGetHistoryBinary(binaryResults)
		if (parsedResults.size() > 1) {
			throw new AugurWorksException('More results returned than expected')
		}
		return parsedResults.first().values.collect { Map result ->
			return new DataSetValue(result.date, result.close)
		}
	}

	private Map historyParametersToMap(HistoryParameters historyParameters) {
		Map parameters = [
			requestvalue: historyParameters.symbolResult.symbol,
			intervaltype: historyParameters.type == 'Day' ? 'DAILY' : 'MINUTE',
			startdate: historyParameters.startDate.format(DATE_FORMAT),
			enddate: historyParameters.endDate.format(DATE_FORMAT),
			requestidentifiertype: 'SYMBOL',
			intervalduration: historyParameters.interval ?: '1'
		]
		return parameters
	}

	private GPathResult makeXmlRequest(String url, Map parameters) {
		String fullUrl = generateUrl(url, parameters)
		return new RestBuilder().get(fullUrl).xml
	}

	private DataInputStream makeBinaryRequest(String url, Map parameters) {
		String fullUrl = generateUrl(url, parameters)
		HttpGet req = new HttpGet(fullUrl)
		HttpClient client = HttpClientBuilder.create().build()
		HttpResponse resp = client.execute(req)
		return new DataInputStream(resp.getEntity().getContent())
	}

	private String generateUrl(String url, Map parameters) {
		parameters.source = SOURCE_ID
		return url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
	}

	Collection<Map> parseGetHistoryBinary(DataInputStream dataInputStream) {
		int symbolCount = dataInputStream.readInt()
		return (1..symbolCount).collect {
			Map map = [:]
			map.symbol = dataInputStream.readUTF()
			Boolean hasError = dataInputStream.readByte().asBoolean()
			if (hasError) {
				map.error = dataInputStream.readUTF()
			} else {
				int chartNum = dataInputStream.readInt()
				map.values = (1..chartNum).collect {
					return [
						close: dataInputStream.readFloat(),
						high: dataInputStream.readFloat(),
						low: dataInputStream.readFloat(),
						open: dataInputStream.readFloat(),
						volume: dataInputStream.readFloat(),
						date: new Date(dataInputStream.readLong())
					]
				}
			}
			return map
		}
	}
}
