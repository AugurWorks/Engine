package com.augurworks.engine.rest

import com.augurworks.engine.helper.DataSetValue

public interface ApiClient {

	Collection<SymbolResult> searchSymbol(String keyword)

	Collection<DataSetValue> getHistory(BarchartHistoryParameters parameters)
}
