package com.augurworks.engine.data

enum SplineType {
	FILL('Fill in data'),
	IGNORE('Ignore extra data')

	public final String name
	public final String description

	SplineType(String description) {
		this.name = this.name()
		this.description = description
	}
}
