package com.augurworks.engine.helper

class DataSetValue {

	String date
	double value

	DataSetValue(String date, String value) {
		this.date = date
		this.value = value.toDouble()
	}

	String getDate() {
		return date
	}

	double getValue() {
		return value
	}
}
