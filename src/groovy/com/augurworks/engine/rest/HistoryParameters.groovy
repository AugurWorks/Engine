package com.augurworks.engine.rest

import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.Unit

class HistoryParameters {

	SymbolResult symbolResult
	Date startDate
	Date endDate
	Unit unit

	Collection<DataSetValue> getHistory() {
		return symbolResult.datasource.apiClient.getHistory(this)
	}
}
