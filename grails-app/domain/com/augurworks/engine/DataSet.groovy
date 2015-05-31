package com.augurworks.engine

class DataSet {

	String name
	String code
	int dataColumn

	static constraints = {
		name()
		code()
		dataColumn min: 0
	}
}
