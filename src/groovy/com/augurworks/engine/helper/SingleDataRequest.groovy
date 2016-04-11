package com.augurworks.engine.helper

import com.augurworks.engine.rest.SymbolResult

class SingleDataRequest {

	SymbolResult symbolResult
	int offset
	Date startDate
	Date endDate
	int minOffset
	int maxOffset
	Aggregation aggregation
	Unit unit

	Collection<DataSetValue> getHistory() {
		return symbolResult.datasource.apiClient.getHistory(this)
	}
}
