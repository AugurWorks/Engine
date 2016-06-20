package com.augurworks.engine.data

import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.model.DataSetValue
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
