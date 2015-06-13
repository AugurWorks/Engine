package com.augurworks.engine

class AlgorithmRequest {

	Date startDate
	Date endDate
	Date dateCreated

	static hasMany = [requestDataSets: RequestDataSet]

	static constraints = {
		startDate()
		endDate()
		dateCreated()
	}

	static mapping = {
		requestDataSets cascade: 'all-delete-orphan'
	}

	String toString() {
		'Request ' + startDate.format('dd/MM/yy') + '-' + endDate.format('dd/MM/yy') + ': ' + requestDataSets*.dataSet*.ticker.join(', ')
	}
}
