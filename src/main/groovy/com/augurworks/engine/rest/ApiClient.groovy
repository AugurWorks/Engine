package com.augurworks.engine.rest

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.model.DataSetValue

interface ApiClient {

	Collection<SymbolResult> searchSymbol(String keyword)

	Collection<DataSetValue> getHistory(SingleDataRequest dataRequest)
}
