package com.augurworks.engine.helper

import com.augurworks.engine.AugurWorksException

enum AlgorithmType {
	ALFRED('Alfred', 'alfred'),
	MACHINE_LEARNING('Machine Learning', 'ml')

	private final String name
	private final String shortName

	AlgorithmType(String name, String shortName) {
		this.name = name
		this.shortName = shortName
	}

	static AlgorithmType findByName(String name) {
		for (AlgorithmType algorithmType : AlgorithmType.values()) {
			if (name == algorithmType.name) {
				return algorithmType
			}
		}
		throw new AugurWorksException('No algorithm type with name ' + name)
	}

	static AlgorithmType findByShortName(String shortName) {
		for (AlgorithmType algorithmType : AlgorithmType.values()) {
			if (shortName == algorithmType.shortName) {
				return algorithmType
			}
		}
		throw new AugurWorksException('No algorithm type with short name ' + shortName)
	}
}
