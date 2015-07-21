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
		return this.toString();
	}

	String stringify() {
		String dateFormat = 'M/d/yyyy'
		String dataSetString = this.requestDataSets.sort { it.dataSet.ticker }.collect { RequestDataSet requestDataSet ->
			return requestDataSet.dataSet.ticker + (requestDataSet.offset >= 0 ? '+' : '') + requestDataSet.offset
		}.join(', ')
		return this.startDate.format(dateFormat) + ' - ' + this.endDate.format(dateFormat) + ': ' + dataSetString
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
