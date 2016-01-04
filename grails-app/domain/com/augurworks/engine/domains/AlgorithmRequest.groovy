package com.augurworks.engine.domains

import groovy.time.TimeCategory

import org.apache.commons.lang.time.DateUtils

import com.augurworks.engine.AugurWorksException

class AlgorithmRequest {

	int startOffset
	int endOffset
	Date dateCreated
	DataSet dependantDataSet
	String unit = 'Day'

	static hasMany = [requestDataSets: RequestDataSet, algorithmResults: AlgorithmResult]

	static constraints = {
		startOffset()
		endOffset()
		dateCreated()
		dependantDataSet()
		unit inList: ['Day', 'Hour']
	}

	static mapping = {
		requestDataSets cascade: 'all-delete-orphan'
		algorithmResults cascade: 'all-delete-orphan'
	}

	String toString() {
		requestDataSets*.toString().sort().join(', ')
	}

	String getName() {
		return this.toString()
	}

	Date getStartDate() {
		return truncateDate('startOffset')
	}

	Date getEndDate() {
		return truncateDate('endOffset')
	}

	Date truncateDate(String field) {
		use(TimeCategory) {
			switch (this.unit) {
				case 'Day':
					return DateUtils.truncate(new Date(), Calendar.DATE) + this[field].days
				case 'Hour':
					return DateUtils.truncate(new Date(), Calendar.HOUR) + this[field].hours
			}
		}
	}

	String stringify() {
		String dataSetString = this.requestDataSets.sort { it.dataSet.ticker }.collect { RequestDataSet requestDataSet ->
			return requestDataSet.dataSet.ticker + (requestDataSet.offset >= 0 ? '+' : '') + requestDataSet.offset
		}.join(', ')
		return (-1 * this.startOffset) + ' to ' + (-1 * this.endOffset) + ' ' + this.unit.toLowerCase() + '(s) ago: ' + dataSetString
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
				aggregation: dataSet.aggregation,
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
