package com.augurworks.engine

import com.augurworks.engine.helper.Global

class PredictedValue {

	Date date
	double value

	static belongsTo = [algorithmResult: AlgorithmResult]

	static constraints = {
		date()
		value()
	}

	String toString() {
		return date.format(Global.DATE_FORMAT) + ': ' + value.round(4)
	}
}
