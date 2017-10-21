package com.augurworks.engine.rest

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import grails.converters.JSON
import grails.plugins.rest.client.RestBuilder
import grails.util.Holders
import groovy.json.JsonBuilder
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.time.DateUtils
import org.joda.time.DateTime

import java.util.concurrent.TimeUnit

class BarchartClient extends RestClient {

	private final String dateFormat = 'yyyyMMddHHmm'

	private final barchartRoot = 'http://ondemand.websol.barchart.com'
	private final historyLookup = barchartRoot + '/getHistory.json'
	private final symbolLookup = barchartRoot + '/getSymbolLookUp.json'

	private final apiKey

	private final LoadingCache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
		@Override
		String load(String key) {
			return barchartRequest(key)
		}
	})

	BarchartClient() {
		apiKey = Holders.config.augurworks.barchart.key
	}

	Collection<SymbolResult> searchSymbol(String keyword) {
		return makeRequest(symbolLookup, [keyword: keyword]).collect { Map result ->
			return new SymbolResult(name: result.symbol.toString() + ' (Future)', symbol: result.symbol, datasource: Datasource.BARCHART)
		}
	}

	Collection<DataSetValue> getHistory(SingleDataRequest dataRequest) {
		Collection<Map> results = makeRequest(dataRequest)
		logStringToS3(dataRequest.symbolResult.datasource.name() + '-' + dataRequest.symbolResult.symbol, new JsonBuilder(results).toPrettyString())
		return results.collect { Map result ->
			Date date = new DateTime(result.timestamp).toDate()
			if (dataRequest.unit == Unit.DAY) {
				date = DateUtils.truncate(date, Calendar.DATE)
			}
			return new DataSetValue(date, result[dataRequest.dataType.name().toLowerCase()])
		}
	}

	private Map historyParametersToMap(SingleDataRequest dataRequest) {
		Map parameters = [
			symbol: dataRequest.symbolResult.symbol,
			type: dataRequest.unit == Unit.DAY ? 'daily' : 'minutes',
			startDate: dataRequest.getOffsetStartDate().format(dateFormat),
			endDate: dataRequest.getOffsetEndDate().format(dateFormat)
		]
		if (dataRequest.unit.interval) {
			parameters.interval = dataRequest.unit == Unit.DAY ? '1' : '15'
		}
		return parameters
	}

	private Collection<Map> makeRequest(SingleDataRequest dataRequest) {
		Map parameters = historyParametersToMap(dataRequest)
		return makeRequest(historyLookup, parameters)
	}

	private Collection<Map> makeRequest(String url, Map parameters) {
		parameters.apikey = apiKey
		String fullUrl = url + '?' + parameters.keySet().grep { String key -> StringUtils.isNotBlank(parameters[key]) }.collect { String key ->
			return key + '=' + URLEncoder.encode(parameters[key])
		}.join('&')
		return JSON.parse(cache.getUnchecked(fullUrl))?.results ?: []
	}

	private String barchartRequest(String url) {
		return new RestBuilder().get(url).text
	}
}
