package com.augurworks.engine.helper

class DataSetValue {

	Date date
	double value

	DataSetValue(Date date, Double value) {
		this.date = date
		this.value = value
	}

	Date getDate() {
		return date
	}

	double getValue() {
		return value
	}

	Map toMap() {
		return [
			date: date.format(Global.C3_DATE_FORMAT),
			value: value
		]
	}
}
