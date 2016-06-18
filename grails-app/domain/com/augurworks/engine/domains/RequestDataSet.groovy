package com.augurworks.engine.domains

import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.Datasource
import com.augurworks.engine.rest.SymbolResult

class RequestDataSet {

	String symbol
	String name
	Datasource datasource
	int offset
	Aggregation aggregation

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		symbol()
		datasource()
		offset()
		aggregation()
	}

	String toString() {
		symbol + (offset >= 0 ? '+' : '') + offset
	}

	SymbolResult toSymbolResult() {
		return new SymbolResult(symbol: symbol, name: name, datasource: datasource)
	}

	static RequestDataSet fromSymbolResult(SymbolResult symbolResult, int offset, Aggregation aggregation, AlgorithmRequest algorithmRequest) {
		return new RequestDataSet(
			symbol: symbolResult.symbol,
			name: symbolResult.name,
			datasource: symbolResult.datasource,
			offset: offset,
			aggregation: aggregation,
			algorithmRequest: algorithmRequest
		)
	}
}
