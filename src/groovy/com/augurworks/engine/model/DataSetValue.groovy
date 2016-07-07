package com.augurworks.engine.model

import com.augurworks.engine.helper.Global

class DataSetValue {

	Date date
	double value

	DataSetValue(Date date, double value) {
		this.date = date
		this.value = value
	}

	Date getDate() {
		return date
	}

	double getValue() {
		return value
	}

	String toString() {
		return this.date.toString() + ': ' + this.value
	}

	Map toMap() {
		return [
			date: date.format(Global.C3_DATE_FORMAT),
			value: value
		]
	}
}
