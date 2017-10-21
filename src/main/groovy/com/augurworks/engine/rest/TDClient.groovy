package com.augurworks.engine.rest

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.helper.TradingHours
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import grails.converters.JSON
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

import java.util.concurrent.TimeUnit

class TDClient extends RestClient {

	private final String dateFormat = 'yyyyMMdd'

	private final tdRoot = 'https://apis.tdameritrade.com/apps/100'
	private final historyLookup = tdRoot + '/PriceHistory'
	private final symbolLookup = tdRoot + '/SymbolLookup'

	private final sourceId

	private final LoadingCache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
		@Override
		String load(String key) {
			return tdJsonResults(key)
		}
	})

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

	Collection<DataSetValue> getHistory(SingleDataRequest dataRequest) {
		Collection<Map> parsedResults = JSON.parse(cache.getUnchecked(generateUrl(historyLookup, dataRequestToMap(dataRequest))))
		logStringToS3(dataRequest.symbolResult.datasource.name() + '-' + dataRequest.symbolResult.symbol, new JsonBuilder(parsedResults).toPrettyString())
		if (parsedResults.size() > 1) {
			throw new AugurWorksException('More results returned than expected')
		}
		return parsedResults.first().values.collect { Map result ->
			return new DataSetValue(new DateTime(result.date).toDate(), result[dataRequest.dataType.name().toLowerCase()])
		}.grep { DataSetValue dataSetValue ->
			return dataRequest.unit == Unit.DAY ? true : TradingHours.tradingMinutesBetween(dataRequest.startDate, dataSetValue.date) % dataRequest.unit.interval == 0
		}
	}

	private Map dataRequestToMap(SingleDataRequest dataRequest) {
		Map parameters = [
			requestvalue: dataRequest.symbolResult.symbol,
			intervaltype: dataRequest.unit == Unit.DAY ? 'DAILY' : 'MINUTE',
			startdate: dataRequest.getOffsetStartDate().format(dateFormat),
			enddate: dataRequest.getOffsetEndDate().format(dateFormat),
			requestidentifiertype: 'SYMBOL',
			intervalduration: dataRequest.unit == Unit.DAY ? '1' : '15'
		]
		return parameters
	}

	private GPathResult makeXmlRequest(String url, Map parameters) {
		String fullUrl = generateUrl(url, parameters)
		return new RestBuilder().get(fullUrl).xml
	}

	private String tdJsonResults(String url) {
		DataInputStream binaryResults = makeBinaryRequest(url)
		return new JsonBuilder(parseGetHistoryBinary(binaryResults)).toString()
	}

	private DataInputStream makeBinaryRequest(String url) {
		HttpGet req = new HttpGet(url)
		HttpClient client = HttpClientBuilder.create().build()
		HttpResponse resp = client.execute(req)
		if (resp.getStatusLine().statusCode != 200) {
			throw new AugurWorksException(EntityUtils.toString(resp.getEntity()))
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
			throw new AugurWorksException('Unexpected number of TD result sets: ' + symbolCount)
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
