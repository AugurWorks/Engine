package com.augurworks.engine

class PredictedValue {

	Date date
	double value

	static belongsTo = [algorithmResult: AlgorithmResult]

	static constraints = {
		date()
		value()
	}
}
