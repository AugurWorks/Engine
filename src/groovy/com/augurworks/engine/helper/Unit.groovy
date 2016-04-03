package com.augurworks.engine.helper

import groovy.time.TimeCategory
import groovy.time.TimeDuration

import java.util.function.BiFunction

enum Unit {
	DAY('Day', 1, filterDate('days', 1)),
	HOUR('Hour', 60, filterDate('minutes', 60)),
	HALF_HOUR('Half Hour', 30, filterDate('minutes', 30))

	String name
	int interval
	BiFunction<Date, Date, Boolean> filterDates

	Unit(String name, int interval, BiFunction<Date, Date, Boolean> filterDates) {
		this.name = name
		this.interval = interval
		this.filterDates = filterDates
	}

	private static BiFunction<Date, Date, Boolean> filterDate(String timeDurationProperty, int interval) {
		return { Date currentDate, Date startDate ->
			use (TimeCategory) {
				TimeDuration timeSinceStart = currentDate - startDate
				return timeSinceStart[timeDurationProperty] % interval == 0
			}
		}
	}
}
