package com.augurworks.engine.helper

import groovy.time.TimeCategory

import com.augurworks.engine.AugurWorksException

class Common {

	static Date calculatePredictionDate(String unit, Date date, int offset) {
		use (TimeCategory) {
			switch (unit) {
				case 'Day':
					return date + offset.days
				case 'Hour':
					return date + offset.hours
				case 'Half Hour':
					return date + (30 * offset).minutes
				default:
					throw new AugurWorksException('Unknown prediction date unit: ' + unit)
			}
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
