package com.augurworks.engine.helper

import groovy.time.TimeCategory

class Common {

	static Date addDaysToDate(Date date, int offset) {
		use (TimeCategory) {
			return date + offset.days
		}
	}

	static Date addHoursToDate(Date date, int offset) {
		use (TimeCategory) {
			return date + offset.hours
		}
	}

	static Date nextWeekday(Date date) {
		if (date[Calendar.DAY_OF_WEEK] == Calendar.SATURDAY) {
			return use(TimeCategory) { date + 2.day }
		}
		if (date[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
			return use(TimeCategory) { date + 1.day }
		}
		return date
	}
}
