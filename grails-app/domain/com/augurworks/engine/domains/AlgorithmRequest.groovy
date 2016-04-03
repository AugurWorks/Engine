package com.augurworks.engine.domains

import groovy.time.TimeCategory

import org.apache.commons.lang.time.DateUtils

import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Datasource

class AlgorithmRequest {

	String name
	int startOffset
	int endOffset
	Date dateCreated
	String dependantSymbol
	String unit = 'Day'
	String cronExpression

	static hasMany = [requestDataSets: RequestDataSet, algorithmResults: AlgorithmResult, cronAlgorithms: AlgorithmType]

	static constraints = {
		name unique: true
		startOffset()
		endOffset()
		dateCreated()
		dependantSymbol()
		unit inList: ['Day', 'Hour', 'Half Hour']
		cronExpression nullable: true
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
		return this.toString() + ': ' + (-1 * this.startOffset) + ' to ' + (-1 * this.endOffset) + ' ' + this.unit.toLowerCase() + '(s) ago'
	}

	void updateFields(Map parameters) {
		parameters.keySet().each { String key ->
			this[key] = parameters[key]
		}
	}

	void updateDataSets(Collection<RequestDataSet> requestDataSets, boolean persist = true) {
		this.requestDataSets?.clear()
		requestDataSets.each { RequestDataSet requestDataSet ->
			this.addToRequestDataSets(requestDataSet)
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
			return requestDataSet.symbol == this.dependantSymbol
		}
		if (matching.size() != 1) {
			throw new AugurWorksException('Prediction data set not found')
		}
		return matching.first()
	}
}
