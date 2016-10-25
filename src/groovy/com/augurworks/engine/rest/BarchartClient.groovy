package com.augurworks.engine.rest

import grails.converters.JSON
import grails.plugin.cache.GrailsCacheManager
import grails.plugin.cache.GrailsValueWrapper
import grails.plugins.rest.client.RestBuilder
import grails.util.Holders
import groovy.json.JsonBuilder

import org.apache.commons.lang.time.DateUtils
import org.joda.time.DateTime

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue

class BarchartClient extends RestClient {

	private final String dateFormat = 'yyyyMMddHHmm'

	private final barchartRoot = 'http://ondemand.websol.barchart.com'
	private final historyLookup = barchartRoot + '/getHistory.json'
	private final symbolLookup = barchartRoot + '/getSymbolLookUp.json'

	private final apiKey

	GrailsCacheManager grailsCacheManager = Holders.grailsApplication.mainContext.getBean('grailsCacheManager')

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
		Date startDate = dataRequest.unit.calculateOffset.apply(dataRequest.startDate, -5)
		Date endDate = dataRequest.unit.calculateOffset.apply(dataRequest.endDate, 3)
		Map parameters = [
			symbol: dataRequest.symbolResult.symbol,
			type: dataRequest.unit == Unit.DAY ? 'daily' : 'minutes',
			startDate: startDate.format(dateFormat),
			endDate: endDate.format(dateFormat)
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
		String fullUrl = url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
		return JSON.parse(barchartRequest(fullUrl))?.results ?: []
	}

	private String barchartRequest(String url) {
		GrailsValueWrapper cache = grailsCacheManager.getCache('externalData').get(url)
		if (cache) {
			return cache.get()
		}
		String result = new RestBuilder().get(url).text
		grailsCacheManager.getCache('externalData').put(url, result)
		return result
	}
}
