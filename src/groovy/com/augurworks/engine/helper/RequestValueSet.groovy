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
}
