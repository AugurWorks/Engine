package com.augurworks.engine.helper

import groovy.time.TimeCategory

class Common {

	static Date addDaysToDate(Date date, int offset) {
		use (TimeCategory) {
			return date + offset.days
		}
	}
}
