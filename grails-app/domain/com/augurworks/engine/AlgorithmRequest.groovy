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

	void updateFields(Map parameters) {
		parameters.keySet().each { String key ->
			this[key] = parameters[key]
		}
	}

	void updateDataSets(Collection<Map> dataSets) {
		this.requestDataSets?.clear()
		dataSets.each { Map dataSet ->
			RequestDataSet requestDataSet = new RequestDataSet([
				dataSet: DataSet.findByTicker(dataSet.name.split(' - ').first()),
				offset: dataSet.offset,
				algorithmRequest: this
			])
			requestDataSet.save()
		}
		this.save(flush: true)
	}
}
