package com.augurworks.engine.helper

import java.util.function.BiFunction

import com.augurworks.engine.AugurWorksException

public enum Aggregation {
	VALUE('Value', this.&identity, this.&identity),
	PERIOD_CHANGE('Period Change', this.&periodChange, this.&periodChangeNormalize),
	PERIOD_PERCENT_CHANGE('Period Percent Change', this.&periodPercentChange, this.&periodPercentChangeNormalize)

	private final String name
	public final BiFunction<Double, Double, Double> aggregate
	public final BiFunction<Double, Double, Double> normalize

	Aggregation(String name, BiFunction<Double, Double, Double> aggregate, BiFunction<Double, Double, Double> normalize) {
		this.name = name
		this.aggregate = aggregate
		this.normalize = normalize
	}

	static Double identity(Double previousValue, Double currentValue) {
		return currentValue
	}

	static Double periodChange(Double previousValue, Double currentValue) {
		if (previousValue == null) {
			return null
		}
		return currentValue - previousValue
	}

	static Double periodPercentChange(Double previousValue, Double currentValue) {
		if (previousValue == null) {
			return null
		}
		if (previousValue == 0) {
			throw new AugurWorksException('Invalid previous value for period percentage change: 0')
		}
		return 100 * (currentValue - previousValue) / previousValue
	}

	static Double periodChangeNormalize(Double previousValue, Double currentValue) {
		if (previousValue == null) {
			return null
		}
		return currentValue + previousValue
	}

	static Double periodPercentChangeNormalize(Double previousValue, Double currentValue) {
		if (previousValue == null) {
			return null
		}
		return previousValue * currentValue / 100 + previousValue
	}
}
