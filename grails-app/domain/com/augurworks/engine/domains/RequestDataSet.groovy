package com.augurworks.engine.domains

import com.augurworks.engine.helper.Aggregation

class RequestDataSet {
	
	DataSet dataSet
	int offset
	String aggregation

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dataSet()
		offset()
		aggregation inList: Aggregation.TYPES
	}

	String toString() {
		dataSet.ticker + (offset >= 0 ? '+' : '') + offset
	}
}
