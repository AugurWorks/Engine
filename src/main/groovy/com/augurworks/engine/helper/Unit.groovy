package com.augurworks.engine.helper

import groovy.time.TimeCategory
import groovy.time.TimeDuration

import java.util.function.BiFunction

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
		return TradingHours.addTradingDays(TradingHours.floorPeriod(startDate, 24 * 60), offset)
	}

	private static Date calculateHour(Date startDate, Integer offset) {
		return TradingHours.addTradingMinutes(TradingHours.floorPeriod(startDate, 60), 60 * offset)
	}

	private static Date calculateHalfHour(Date startDate, Integer offset) {
		return TradingHours.addTradingMinutes(TradingHours.floorPeriod(startDate, 30), 30 * offset)
	}

	private static Date calculateFifteenMinutes(Date startDate, Integer offset) {
		return TradingHours.addTradingMinutes(TradingHours.floorPeriod(startDate, 15), 15 * offset)
	}
}
