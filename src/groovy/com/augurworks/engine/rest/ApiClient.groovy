package com.augurworks.engine.rest

import com.augurworks.engine.helper.DataSetValue
import com.augurworks.engine.helper.SingleDataRequest

interface ApiClient {

	Collection<SymbolResult> searchSymbol(String keyword)

	Collection<DataSetValue> getHistory(SingleDataRequest dataRequest)
}
