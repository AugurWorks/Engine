package com.augurworks.engine.controllers

import grails.core.GrailsApplication
import groovy.time.TimeCategory

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.TrainingStat
import com.augurworks.engine.stats.TrainingStage;

class HomeController {

	GrailsApplication grailsApplication

	Map timeRanges = [
		'Last Week': 7 * 24 * 60,
		'Last Day': 24 * 60,
		'Last Hour': 60
	]

	Collection<Map> timeRangeCollection = timeRanges.keySet().collect { String key ->
		return [
			key: key,
			value: timeRanges[key]
		]
	}

	def index() {
		if (!grailsApplication.config.graphs.on.toBoolean()) {
			return [recentRuns: [], graphsOn: false]
		}
		Date lastHour = use(TimeCategory) { new Date() - 1.hour }
		Collection<AlgorithmResult> recentRuns = AlgorithmResult.findAllByDateCreatedGreaterThanOrComplete(lastHour, false, [sort: 'dateCreated', max: 10])
		return [recentRuns: recentRuns, graphsOn: true]
	}

	def dashboard(Integer offset) {
		Collection<AlgorithmResult> runs = TrainingStat.findAllByDateCreatedGreaterThanAndTrainingStage(getOffsetDate(offset ?: timeRanges['Last Day']), TrainingStage.DONE)
		[runs: runs, timeRanges: timeRangeCollection, timeRange: offset ?: timeRanges['Last Day']]
	}

	private Date getOffsetDate(Integer offset) {
		use (TimeCategory) {
			return new Date() - offset.minutes
		}
	}
}
