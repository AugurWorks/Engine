package com.augurworks.engine.rest

import com.augurworks.engine.helper.Datasource

class SymbolResult {

	String name
	String symbol
	Datasource datasource

	Map toResultMap() {
		return [
			name: name,
			value: symbol + '-' + datasource.toString()
		]
	}

	static SymbolResult fromDropdownValue(String value) {
		Collection<String> parts = value.split('-')
		return new SymbolResult(symbol: parts[0], Datasource[parts[1]])
	}
}
