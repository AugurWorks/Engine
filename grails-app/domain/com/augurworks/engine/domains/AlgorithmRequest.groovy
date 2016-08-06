package com.augurworks.engine.domains

import com.augurworks.engine.data.SplineType
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlfredEnvironment
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Unit

class AlgorithmRequest {

	String name
	int startOffset
	int endOffset
	Date dateCreated
	String dependantSymbol
	Unit unit = Unit.DAY
	String cronExpression
	AlfredEnvironment alfredEnvironment = AlfredEnvironment.LAMBDA
	SplineType splineType = SplineType.FILL

	static hasMany = [requestDataSets: RequestDataSet, algorithmResults: AlgorithmResult, cronAlgorithms: AlgorithmType]

	static constraints = {
		name unique: true
		startOffset()
		endOffset()
		dateCreated()
		dependantSymbol()
		unit()
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
		return this.unit.calculateOffset.apply(now, this[field])
	}

	String stringify() {
		return this.toString() + ': ' + (-1 * this.startOffset) + ' to ' + (-1 * this.endOffset) + ' ' + this.unit.name().toLowerCase() + '(s) ago'
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
		Collection<String> dependentFields = this.dependantSymbol.split(' - ')
		Collection<RequestDataSet> matching = this.requestDataSets.grep { RequestDataSet requestDataSet ->
			return requestDataSet.symbol == dependentFields[0] && requestDataSet.dataType.name() == dependentFields[1]
		}
		if (matching.size() == 0) {
			throw new AugurWorksException('Prediction data set not found')
		}
		if (matching.size() > 1) {
			throw new AugurWorksException('Multiple prediction data sets found')
		}
		return matching.first()
	}
}
