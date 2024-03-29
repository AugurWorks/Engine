package com.augurworks.engine.rest

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.exceptions.DataAccessException
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.TradingHours
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue
import grails.converters.JSON
import grails.plugin.cache.Cacheable
import grails.plugins.rest.client.RestBuilder
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.util.slurpersupport.GPathResult
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.joda.time.DateTime

class TDClient extends RestClient {

	private final String dateFormat = 'yyyyMMdd'

	private final tdRoot = 'https://apis.tdameritrade.com/apps/100'
	private final historyLookup = tdRoot + '/PriceHistory'
	private final symbolLookup = tdRoot + '/SymbolLookup'

	private final sourceId

	TDClient() {
		sourceId = Holders.config.augurworks.td.key
	}

	Collection<SymbolResult> searchSymbol(String keyword) {
		Collection<GPathResult> xmlResults = makeXmlRequest(symbolLookup, [matchstring: keyword]).depthFirst().findAll { GPathResult result ->
			return result.name() == 'symbol-result'
		}
		return xmlResults.collect { GPathResult result ->
			return new SymbolResult(name: result.getProperty('symbol').toString() + ' (Stock)', symbol: result.getProperty('symbol'), datasource: Datasource.TD)
		}
	}

	Collection<DataSetValue> getHistory(SingleDataRequest dataRequest, boolean getNowOnly) {
		statsdClient.increment('count.data.td.request')
		Collection<Map> parsedResults = JSON.parse(tdJsonResults(generateUrl(historyLookup, dataRequestToMap(dataRequest, getNowOnly))))
		logStringToS3(dataRequest.symbolResult.datasource.name() + '-' + dataRequest.symbolResult.symbol, new JsonBuilder(parsedResults).toPrettyString())
		if (parsedResults.size() != 1) {
			throw new DataAccessException('More results returned than expected: ' + parsedResults.size())
		}
		return parsedResults.first().values.collect { Map result ->
			return new DataSetValue(new DateTime(result.date).toDate(), result[dataRequest.dataType.name().toLowerCase()])
		}.grep { DataSetValue dataSetValue ->
			return dataRequest.unit == Unit.DAY ? true : TradingHours.tradingMinutesBetween(dataRequest.startDate, dataSetValue.date) % dataRequest.unit.interval == 0 && TradingHours.isMarketOpen(dataSetValue.date)
		}
	}

	private Map dataRequestToMap(SingleDataRequest dataRequest, boolean getNowOnly) {
		Map parameters = [
			requestvalue: dataRequest.symbolResult.symbol,
			intervaltype: dataRequest.unit == Unit.DAY ? 'DAILY' : 'MINUTE',
			startdate: getOffsetStartDate(dataRequest, getNowOnly).format(dateFormat),
			enddate: getOffsetEndDate(dataRequest, getNowOnly).format(dateFormat),
			requestidentifiertype: 'SYMBOL',
			intervalduration: dataRequest.unit == Unit.DAY ? '1' : '5'
		]
		return parameters
	}

	private GPathResult makeXmlRequest(String url, Map parameters) {
		String fullUrl = generateUrl(url, parameters)
		return new RestBuilder().get(fullUrl).xml
	}

	@Cacheable('externalData')
	private String tdJsonResults(String url) {
		long startTime = System.currentTimeMillis()
		try {
			DataInputStream binaryResults = makeBinaryRequest(url)
			return new JsonBuilder(parseGetHistoryBinary(binaryResults)).toString()
		} finally {
			statsdClient.recordGaugeValue('histogram.data.td.request.time', System.currentTimeMillis() - startTime, 'un:ms')
		}
	}

	private DataInputStream makeBinaryRequest(String url) {
		HttpGet req = new HttpGet(url)
		HttpClient client = HttpClientBuilder.create().build()
		HttpResponse resp = client.execute(req)
		if (resp.getStatusLine().statusCode != 200) {
			throw new DataAccessException(EntityUtils.toString(resp.getEntity()))
		}
		return new DataInputStream(resp.getEntity().getContent())
	}

	private String generateUrl(String url, Map parameters) {
		parameters.source = sourceId
		return url + '?' + parameters.collect { String key, String value ->
			return key + '=' + URLEncoder.encode(value)
		}.join('&')
	}

	Collection<Map> parseGetHistoryBinary(DataInputStream dataInputStream) {
		int symbolCount = dataInputStream.readInt()
		if (symbolCount != 1) {
			throw new DataAccessException('Unexpected number of TD result sets: ' + symbolCount)
		}
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
