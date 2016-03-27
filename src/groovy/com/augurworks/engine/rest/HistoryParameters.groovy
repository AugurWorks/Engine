package com.augurworks.engine.rest

import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.helper.Global

class HistoryParameters {

	SymbolResult symbolResult
	String type
	Date startDate
	Date endDate
	int interval
	
	Map toParameters() {
		Map parameters = [
			symbol: symbolResult.symbol,
			type: type,
			startDate: startDate.format(Global.BARCHART_FORMAT),
			endDate: endDate.format(Global.BARCHART_FORMAT)
		]
		if (interval) {
			parameters.interval = interval
		}
		return parameters
	}

	Collection<DataSet> getHistory() {
		return symbolResult.datasource.apiClient.getHistory(this)
	}
}
