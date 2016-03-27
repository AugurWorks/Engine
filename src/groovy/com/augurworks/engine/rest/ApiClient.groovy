package com.augurworks.engine.rest

import com.augurworks.engine.helper.DataSetValue

interface ApiClient {

	Collection<SymbolResult> searchSymbol(String keyword)

	Collection<DataSetValue> getHistory(HistoryParameters parameters)
}
