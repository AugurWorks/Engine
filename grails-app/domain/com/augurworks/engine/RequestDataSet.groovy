package com.augurworks.engine

class RequestDataSet {
	
	DataSet dataSet
	int offset

	static belongsTo = [algorithmRequest: AlgorithmRequest]

	static constraints = {
		dataSet()
		offset()
	}

	String toString() {
		dataSet.ticker + (offset >= 0 ? '+' : '') + offset
	}
}
