package com.augurworks.engine.rest

import com.augurworks.engine.helper.Global

class HistoryParameters {

	String symbol
	String type
	Date startDate
	Date endDate
	int interval

	Map toParameters() {
		Map parameters = [
			symbol: symbol,
			type: type,
			startDate: startDate.format(Global.BARCHART_FORMAT),
			endDate: endDate.format(Global.BARCHART_FORMAT)
		]
		if (interval) {
			parameters.interval = interval
		}
		return parameters
	}
}
