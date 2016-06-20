package com.augurworks.engine.data

import java.util.function.Function

enum SplineType {
	FILL('Fill in data', this.&unique),
	IGNORE('Ignore extra data', this.&allContainDate)

	public final String name
	public final String description
	public final Function<Collection<Collection<Date>>, Collection<Date>> reduceDates

	SplineType(String description, Function reduceDates) {
		this.name = this.name()
		this.description = description
		this.reduceDates = reduceDates
	}

	static Collection<Date> unique(Collection<Collection<Date>> allDates) {
		return allDates.flatten().sort().unique()
	}

	static Collection<Date> allContainDate(Collection<Collection<Date>> allDates) {
		Collection<Date> uniqueDates = unique(allDates)
		return uniqueDates.grep { Date date ->
			return allDates.collect { Collection<Date> dateSet ->
				return dateSet.contains(date)
			}.every()
		}
	}
}
