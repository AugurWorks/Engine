package com.augurworks.engine.helper

enum DataType {
	CLOSE('Close'),
	HIGH('High'),
	LOW('Low'),
	OPEN('Open'),
	VOLUME('Volume')

	private final String name

	DataType(String name) {
		this.name = name
	}

	private static final Map<String, DataType> DATA_TYPE_MAP = [:]
	static {
		values().each { DataType dataType ->
			DATA_TYPE_MAP[dataType.name] = dataType
		}
	}

	static DataType findByName(String name) {
		return DATA_TYPE_MAP[name]
	}
}
