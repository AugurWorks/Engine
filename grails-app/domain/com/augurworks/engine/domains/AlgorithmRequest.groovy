package com.augurworks.engine.domains

import groovy.time.TimeCategory

import org.apache.commons.lang.time.DateUtils

import com.augurworks.engine.AugurWorksException

class AlgorithmRequest {

	String name
	int startOffset
	int endOffset
	Date dateCreated
	DataSet dependantDataSet
	String unit = 'Day'

	static hasMany = [requestDataSets: RequestDataSet, algorithmResults: AlgorithmResult]

	static constraints = {
		name unique: true
		startOffset()
		endOffset()
		dateCreated()
		dependantDataSet()
		unit inList: ['Day', 'Hour', 'Half Hour']
	}

	static mapping = {
		requestDataSets cascade: 'all-delete-orphan'
		algorithmResults cascade: 'all-delete-orphan'
	}

	String toString() {
		name
	}

	String getName() {
		return this.toString()
	}

	Date getStartDate(Date now = new Date()) {
		return truncateDate('startOffset', now)
	}

	Date getEndDate(Date now = new Date()) {
		return truncateDate('endOffset', now)
	}

	Date truncateDate(String field, Date now) {
		use(TimeCategory) {
			switch (this.unit) {
				case 'Day':
					return DateUtils.truncate(now, Calendar.DATE) + this[field].days
				case 'Hour':
					Date date = DateUtils.truncate(now, Calendar.HOUR) + this[field].hours
					if (now[Calendar.MINUTE] >= 30) {
						date[Calendar.MINUTE] = 30
					}
					return date
				case 'Half Hour':
					Date date = DateUtils.truncate(now, Calendar.HOUR) + (30 * this[field]).minutes
					if (now[Calendar.MINUTE] >= 30) {
						date[Calendar.MINUTE] = 30
					}
					return date
			}
		}
	}

	String stringify() {
		String dataSetString = this.requestDataSets.sort { it.dataSet.ticker }.collect { RequestDataSet requestDataSet ->
			return requestDataSet.dataSet.ticker + (requestDataSet.offset >= 0 ? '+' : '') + requestDataSet.offset
		}.join(', ')
		return this.toString() + ': ' + (-1 * this.startOffset) + ' to ' + (-1 * this.endOffset) + ' ' + this.unit.toLowerCase() + '(s) ago'
	}

	void updateFields(Map parameters) {
		parameters.keySet().each { String key ->
			this[key] = parameters[key]
		}
	}

	void updateDataSets(Collection<Map> dataSets, boolean persist = true) {
		this.requestDataSets?.clear()
		dataSets.each { Map dataSet ->
			this.addToRequestDataSets([
				dataSet: DataSet.findByTicker(dataSet.name.split(' - ').first()),
				offset: dataSet.offset,
				aggregation: dataSet.aggregation
			])
		}
		if (persist) {
			this.save(flush: true)
		}
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
