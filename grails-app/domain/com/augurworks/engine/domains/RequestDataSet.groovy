package com.augurworks.engine.domains

import com.augurworks.engine.helper.Aggregation

class RequestDataSet {

	DataSet dataSet
	int offset
	Aggregation aggregation

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dataSet()
		offset()
		aggregation()
	}

	String toString() {
		dataSet.ticker + (offset >= 0 ? '+' : '') + offset
	}
}
