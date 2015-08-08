package com.augurworks.engine.helper

import com.augurworks.engine.AugurWorksException

class Aggregation {

	static final Collection<String> TYPES = ['Value', 'Period Change', 'Period Percent Change']

	static Double aggregate(String type, Double previousValue, double currentValue) {
		switch(type) {
			case 'Value':
				return currentValue
			case 'Period Change':
				return previousValue != null ? currentValue - previousValue : null
			case 'Period Percent Change':
				return previousValue != null ? periodPercentChange(previousValue, currentValue) : null
			default:
				throw new AugurWorksException('Invalid aggregation type: ' + type)
		}
	}

	static double periodPercentChange(double previousValue, double currentValue) {
		if (previousValue == 0) {
			throw new AugurWorksException('Invalid previous value for period percentage change: 0')
		}
		return 100 * (currentValue - previousValue) / previousValue
	}
}
