package com.augurworks.engine

class AlgorithmRequest {

	Date startDate
	Date endDate

	static hasMany = [requestDataSets: RequestDataSet]

	static constraints = {
		startDate()
		endDate()
	}

	static mapping = {
		autoTimestamp true
	}

	String toString() {
		'Request ' + startDate.format('dd/MM/yy') + '-' + endDate.format('dd/MM/yy') + ': ' + requestDataSets*.dataSet*.ticker.join(', ')
	}
}
