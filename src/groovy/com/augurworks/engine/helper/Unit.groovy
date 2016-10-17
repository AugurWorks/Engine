package com.augurworks.engine.helper

import groovy.time.TimeCategory
import groovy.time.TimeDuration

import java.util.function.BiFunction

import org.apache.commons.lang.time.DateUtils

enum Unit {
	DAY('Day', 1, filterDate('days', 1), this.&calculateDay),
	HOUR('Hour', 60, filterDate('minutes', 60), this.&calculateHour),
	HALF_HOUR('Half Hour', 30, filterDate('minutes', 30), this.&calculateHalfHour),
	FIFTEEN_MINUTES('Fifteen Minutes', 15, filterDate('minutes', 15), this.&calculateFifteenMinutes)

	String name
	int interval
	BiFunction<Date, Date, Boolean> filterDates
	BiFunction<Date, Integer, Date> calculateOffset

	Unit(String name, int interval, BiFunction<Date, Date, Boolean> filterDates, BiFunction<Date, Integer, Date> calculateOffset) {
		this.name = name
		this.interval = interval
		this.filterDates = filterDates
		this.calculateOffset = calculateOffset
	}

	private static BiFunction<Date, Date, Boolean> filterDate(String timeDurationProperty, Integer interval) {
		return { Date currentDate, Date startDate ->
			use (TimeCategory) {
				TimeDuration timeSinceStart = currentDate - startDate
				return timeSinceStart[timeDurationProperty] % interval == 0
			}
		}
	}

	private static Date calculateDay(Date startDate, Integer offset) {
		use(TimeCategory) {
			return DateUtils.truncate(startDate, Calendar.DATE) + offset.days
		}
	}

	private static Date calculateHour(Date startDate, Integer offset) {
		return calculateMinuteOffset(startDate, offset, 60)
	}

	private static Date calculateHalfHour(Date startDate, Integer offset) {
		return calculateMinuteOffset(startDate, offset, 30)
	}

	private static Date calculateFifteenMinutes(Date startDate, Integer offset) {
		return calculateMinuteOffset(startDate, offset, 15)
	}

	private static Date calculateMinuteOffset(Date startDate, Integer offset, Integer minutes) {
		use(TimeCategory) {
			Date date = DateUtils.truncate(startDate, Calendar.HOUR)
			date += (15 * (Integer) Math.floor(startDate[Calendar.MINUTE] / 15)).minutes
			date += (minutes * offset).minutes
			return date
		}
	}
}
