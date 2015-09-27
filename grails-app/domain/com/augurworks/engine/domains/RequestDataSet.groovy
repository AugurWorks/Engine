package com.augurworks.engine.domains

import com.augurworks.engine.helper.Aggregations

class RequestDataSet {

	DataSet dataSet
	int offset
	String aggregation

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dataSet()
		offset()
		aggregation inList: Aggregations.TYPES
	}

	String toString() {
		dataSet.ticker + (offset >= 0 ? '+' : '') + offset
	}
}
