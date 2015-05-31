package com.augurworks.engine

class DataSet {

	String ticker
	String name
	String code
	int dataColumn

	static constraints = {
		ticker()
		name unique: true
		code()
		dataColumn min: 0
	}

	String toString() {
		name
	}
}
