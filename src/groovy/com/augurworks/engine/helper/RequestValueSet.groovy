package com.augurworks.engine.helper

import com.augurworks.engine.AugurWorksException

class RequestValueSet {

	String name
	int offset
	Collection<DataSetValue> values

	RequestValueSet(String name, int offset, Collection<DataSetValue> values) {
		this.name = name
		this.offset = offset
		this.values = values
	}

	String getName() {
		return name
	}

	String getOffset() {
		return offset
	}

	Collection<DataSetValue> getValues() {
		return values
	}

	Collection<Date> getDates() {
		return this.values*.date
	}

	RequestValueSet aggregateValues(String aggregationType) {
		Collection<DataSetValue> values = this.values
		Collection<DataSetValue> newValues = []
		for (int i = 0; i < values.size(); i++) {
			Double previousValue = i == 0 ? null : values[i - 1].value
			DataSetValue current = values[i]
			Double newValue = Aggregation.aggregate(aggregationType, previousValue, current.value)
			if (newValue != null) {
				newValues.push(new DataSetValue(current.date, newValue))
			}
		}
		this.values = newValues
		return this
	}

	RequestValueSet filterValues(Date startDate, Date endDate, int minOffset, int maxOffset) {
		Collection<DataSetValue> values = this.values
		int startIndex = values.findIndexOf { it.date == startDate }
		int endIndex = values.findIndexOf { it.date == endDate }
		if (startIndex == -1 || endIndex == -1 || startIndex + minOffset < 0 || endIndex + maxOffset > values.size() - 1) {
			throw new AugurWorksException(this.name + ' does not contain data for the requested range ')
		}
		this.values = values[(startIndex + minOffset)..(endIndex + maxOffset)]
		return this
	}

	RequestValueSet fillOutValues(Collection<Date> allDates) {
		Collection<DataSetValue> values = this.values
		if (values.size() == 0 || allDates.size() == 0 || values.first().date != allDates.first()) {
			throw new AugurWorksException('Invalid fill out values parameters')
		}
		allDates[1..-1].eachWithIndex { Date date, int index ->
			DataSetValue dataSetValue = values[index + 1]
			if (!dataSetValue || dataSetValue.date != date) {
				DataSetValue oldDataSetValue = values[index]
				DataSetValue newDataSetValue = new DataSetValue(date, oldDataSetValue.value)
				values = values.plus(index + 1, newDataSetValue)
			}
		}
		this.values = values
		return this
	}

	RequestValueSet reduceValueRange(Date startDate, Date endDate, int predictionOffset) {
		int minOffset = Math.min(this.offset, predictionOffset)
		int maxOffset = Math.max(this.offset, predictionOffset)
		return this.filterValues(startDate, endDate, minOffset, maxOffset)
	}

	RequestValueSet reduceValueRange(Date startDate, Date endDate) {
		return this.filterValues(startDate, endDate, this.offset, this.offset)
	}

	String toString() {
		return this.name + (this.offset >=0 ? '+' : '-') + this.offset
	}

	Map toMap() {
		return [
			name: name,
			offset: offset,
			values: values*.toMap()
		]
	}
}
