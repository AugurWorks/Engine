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
}
