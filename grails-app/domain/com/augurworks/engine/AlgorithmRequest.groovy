package com.augurworks.engine

import com.augurworks.engine.helper.Global

class AlgorithmRequest {

	Date startDate
	Date endDate
	Date dateCreated
	DataSet dependantDataSet

	static hasMany = [requestDataSets: RequestDataSet, algorithmResults: AlgorithmResult]

	static constraints = {
		startDate()
		endDate()
		dateCreated()
		dependantDataSet()
	}

	static mapping = {
		requestDataSets cascade: 'all-delete-orphan'
		algorithmResults cascade: 'all-delete-orphan'
	}

	String toString() {
		requestDataSets*.toString().sort().join(', ')
	}

	String getName() {
		return this.toString();
	}

	String stringify() {
		String dataSetString = this.requestDataSets.sort { it.dataSet.ticker }.collect { RequestDataSet requestDataSet ->
			return requestDataSet.dataSet.ticker + (requestDataSet.offset >= 0 ? '+' : '') + requestDataSet.offset
		}.join(', ')
		return this.startDate.format(Global.DATE_FORMAT) + ' - ' + this.endDate.format(Global.DATE_FORMAT) + ': ' + dataSetString
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

	Collection<RequestDataSet> getIndependentRequestDataSets() {
		return this.requestDataSets - [this.dependentRequestDataSet]
	}

	int getPredictionOffset() {
		return this.dependentRequestDataSet.offset
	}

	RequestDataSet getDependentRequestDataSet() {
		Collection<RequestDataSet> matching = this.requestDataSets.grep { RequestDataSet requestDataSet ->
			return requestDataSet.dataSet == this.dependantDataSet
		}
		if (matching.size() != 1) {
			throw new AugurWorksException('Prediction data set not found')
		}
		return matching.first()
	}
}
