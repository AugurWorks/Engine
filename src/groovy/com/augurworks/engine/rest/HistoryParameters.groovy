package com.augurworks.engine.rest

import com.augurworks.engine.domains.DataSet

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
