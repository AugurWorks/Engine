package com.augurworks.engine.helper

import java.util.function.BiFunction

enum Unit {
	DAY('Day', 1, this.&calculateDay),
	HOUR('Hour', 60, this.&calculateHour),
	HALF_HOUR('Half Hour', 30, this.&calculateHalfHour),
	FIFTEEN_MINUTES('Fifteen Minutes', 15, this.&calculateFifteenMinutes),
	FIVE_MINUTES('Five Minutes', 5, this.&calculateFiveMinutes)

	String name
	int interval
	BiFunction<Date, Integer, Date> calculateOffset

	Unit(String name, int interval, BiFunction<Date, Integer, Date> calculateOffset) {
		this.name = name
		this.interval = interval
		this.calculateOffset = calculateOffset
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

	private static Date calculateFiveMinutes(Date startDate, Integer offset) {
		return TradingHours.addTradingMinutes(TradingHours.floorPeriod(startDate, 5), 5 * offset)
	}
}
