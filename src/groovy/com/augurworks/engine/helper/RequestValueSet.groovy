package com.augurworks.engine.helper

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
}
