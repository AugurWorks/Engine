package com.augurworks.engine.model

import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.Aggregation
import com.augurworks.engine.helper.DataType
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.Unit

class RequestValueSet {

	String name
	DataType dataType
	int offset
	Collection<DataSetValue> values

	RequestValueSet(String name, DataType dataType, int offset, Collection<DataSetValue> values) {
		this.name = name
		this.dataType = dataType
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

	RequestValueSet aggregateValues(Aggregation aggregationType) {
		Collection<DataSetValue> values = this.values
		Collection<DataSetValue> newValues = []
		for (int i = 0; i < values.size(); i++) {
			Double previousValue = i == 0 ? null : values[i - 1].value
			DataSetValue current = values[i]
			Double newValue = aggregationType.aggregate.apply(previousValue, current.value)?.round(3)
			if (newValue != null) {
				newValues.push(new DataSetValue(current.date, newValue))
			}
		}
		this.values = newValues
		return this
	}

	RequestValueSet filterValues(Unit unit, Date startDate, Date endDate, int minOffset, int maxOffset) {
		Collection<DataSetValue> values = this.values.grep { DataSetValue dataSetValue ->
			return unit.filterDates.apply(dataSetValue.date, startDate)
		}
		int startIndex = values.findIndexOf { it.date == startDate }
		int endIndex = values.findIndexOf { it.date == endDate }
		Collection<String> errors = []
		Collection<String> debugs = []
		if (startIndex == -1 || endIndex == -1) {
			debugs.push('Failed to get data for ' + this.name)
		}
		if (startIndex == -1) {
			String first = this.values.size() > 0 ? this.values.first().date : 'None'
			errors.add(this.name + ' does not contain data for the start date, ' + startDate.format(Global.ERROR_DATE_FORMAT))
			debugs.push('Start date needed: ' + startDate)
			debugs.push('First available date: ' + first)
			errors.addAll(['Start date needed: ' + startDate, 'First available date: ' + first])
		}
		if (endIndex == -1) {
			String last = this.values.size() > 0 ? this.values.last().date : 'None'
			errors.add(this.name + ' does not contain data for the end date, ' + endDate.format(Global.ERROR_DATE_FORMAT))
			debugs.push('End date needed: ' + endDate)
			debugs.push('Last available date: ' + last)
			errors.addAll(['End date needed: ' + endDate, 'Last available date: ' + last])
		}
		if (startIndex + minOffset < 0) {
			errors.add(this.name + ' does not contain data all offsets before the start date')
		}
		if (endIndex + maxOffset > values.size() - 1) {
			errors.add(this.name + ' does not contain data all offsets after the end date')
		}
		if (debugs.size() != 0) {
			log.debug(debugs.join('\n'))
		}
		if (errors.size() != 0) {
			throw new AugurWorksException(errors.join('<br />'))
		}
		this.values = values[(startIndex + minOffset)..(endIndex + maxOffset)]
		return this
	}

	RequestValueSet fillOutValues(Collection<Date> allDates) {
		Collection<DataSetValue> values = this.values
		if (values.size() == 0) {
			throw new AugurWorksException(this.name + ' does not contain any values for the given range')
		}
		if (allDates.size() == 0) {
			throw new AugurWorksException('The date range provided for ' + this.name + ' does not contain any dates')
		}
		if (allDates.size() > values.size()) {
			allDates[1..-1].eachWithIndex { Date date, int index ->
				DataSetValue dataSetValue = values[index + 1]
				if (!dataSetValue || dataSetValue.date != date) {
					DataSetValue oldDataSetValue = values[index]
					DataSetValue newDataSetValue = new DataSetValue(date, oldDataSetValue.value)
					values = values.plus(index + 1, newDataSetValue)
				}
			}
			this.values = values
		} else if (allDates.size() < this.values.size()) {
			this.values = values.grep { DataSetValue dataSetValue ->
				return allDates.contains(dataSetValue.date)
			}
		}
		return this
	}

	RequestValueSet reduceValueRange(Unit unit, Date startDate, Date endDate, int predictionOffset = this.offset) {
		int minOffset = Math.min(this.offset, predictionOffset)
		int maxOffset = Math.max(this.offset, predictionOffset)
		return this.filterValues(unit, startDate, endDate, minOffset, maxOffset)
	}

	String toString() {
		return this.name + (this.offset >=0 ? '+' : '-') + this.offset
	}

	Map toMap() {
		return [
			name: name + ' - ' + dataType.name()[0],
			offset: offset,
			values: values*.toMap()
		]
	}
}
