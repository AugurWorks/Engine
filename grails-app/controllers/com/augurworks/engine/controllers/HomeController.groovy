package com.augurworks.engine.controllers

import groovy.time.TimeCategory

import com.augurworks.engine.domains.AlgorithmResult

class HomeController {

	def index() {
		Date lastHour = use(TimeCategory) { new Date().minus(1.hour) }
		Collection<AlgorithmResult> recentRuns = AlgorithmResult.findAllByDateCreatedGreaterThan(lastHour)
		[recentRuns: recentRuns]
	}
}
