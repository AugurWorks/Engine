package com.augurworks.engine

class AlgorithmRequest {

	Date startDate
	Date endDate
	Date dateCreated
	DataSet dependantDataSet

	static hasMany = [requestDataSets: RequestDataSet]

	static constraints = {
		startDate()
		endDate()
		dateCreated()
		dependantDataSet()
	}

	static mapping = {
		requestDataSets cascade: 'all-delete-orphan'
	}

	String toString() {
		requestDataSets*.dataSet*.ticker.join(', ')
	}

	String getName() {
		return this.toString()
	}
}
