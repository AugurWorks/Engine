package com.augurworks.engine.helper

import com.augurworks.engine.AugurWorksException
import java.util.Date;

class RequestValueSet {

	String name
	Collection<DataSetValue> values

	RequestValueSet(String name, Collection<DataSetValue> values) {
		this.name = name
		this.values = values
	}

	String getName() {
		return name
	}

	Collection<DataSetValue> getValues() {
		return values
	}

	Collection<String> getDates() {
		return this.values*.date
	}

	RequestValueSet filterValues(Date startDate, Date endDate, int minOffset, int maxOffset) {
		Collection<DataSetValue> values = this.values
		int startIndex = values.findIndexOf { it.date == startDate.format('yyyy-MM-dd') }
		int endIndex = values.findIndexOf { it.date == endDate.format('yyyy-MM-dd') }
		if (startIndex == -1 || endIndex == -1 || startIndex + minOffset < 0 || endIndex + maxOffset > values.size() - 1) {
			throw new AugurWorksException(this.name + ' does not contain data for the requested range ')
		}
		this.values = values[(startIndex + minOffset)..(endIndex + maxOffset)]
		return this
	}

	RequestValueSet fillOutValues(Collection<String> allDates) {
		Collection<DataSetValue> values = this.values
		if (values.size() == 0 || allDates.size() == 0 || values.first().date != allDates.first()) {
			throw new AugurWorksException('Invalid fill out values parameters')
		}
		allDates[1..-1].eachWithIndex { String date, int index ->
			DataSetValue dataSetValue = values[index + 1]
			if (!dataSetValue || dataSetValue.date != date) {
				DataSetValue oldDataSetValue = values[index]
				DataSetValue newDataSetValue = new DataSetValue(date, oldDataSetValue.value.toString())
				values = values.plus(index + 1, newDataSetValue)
			}
		}
		this.values = values
		return this
	}
}
