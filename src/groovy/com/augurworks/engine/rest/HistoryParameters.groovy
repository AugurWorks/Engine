package com.augurworks.engine.rest

import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.helper.Global

class HistoryParameters {

	SymbolResult symbolResult
	String type
	Date startDate
	Date endDate
	int interval

	Collection<DataSet> getHistory() {
		return symbolResult.datasource.apiClient.getHistory(this)
	}
}
