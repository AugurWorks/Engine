package com.augurworks.engine.helper

enum Unit {
	DAY('Day', 1),
	HOUR('Hour', 60),
	HALF_HOUR('Half Hour', 30)

	String name
	int interval

	Unit(String name, int interval) {
		this.name = name
		this.interval = interval
	}
}
