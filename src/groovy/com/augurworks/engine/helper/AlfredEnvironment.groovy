package com.augurworks.engine.helper

import com.augurworks.engine.exceptions.AugurWorksException

enum AlfredEnvironment {
	LAMBDA('Lambda (5 min cap)'),
	DOCKER('Docker (1 hr cap)')

	private final String name

	AlfredEnvironment(String name) {
		this.name = name
	}

	static AlfredEnvironment findByName(String name) {
		for (AlfredEnvironment alfredEnvironment : AlfredEnvironment.values()) {
			if (name == alfredEnvironment.name) {
				return alfredEnvironment
			}
		}
		throw new AugurWorksException('No alfred environments with name ' + name)
	}
}
