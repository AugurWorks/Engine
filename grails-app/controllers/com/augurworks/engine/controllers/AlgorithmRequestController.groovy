package com.augurworks.engine.controllers

import grails.converters.JSON

import org.quartz.CronExpression

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.SplineRequest
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.services.AlfredService
import com.augurworks.engine.services.AutoKickoffService
import com.augurworks.engine.services.AutomatedService
import com.augurworks.engine.services.DataRetrievalService
import com.augurworks.engine.services.MachineLearningService

class AlgorithmRequestController {

	MachineLearningService machineLearningService
	AlfredService alfredService
	DataRetrievalService dataRetrievalService
	AutomatedService automatedService
	AutoKickoffService autoKickoffService

	def index() {
		[requests: AlgorithmRequest.list()]
	}

	def show(AlgorithmRequest algorithmRequest) {
		if (!algorithmRequest) {
			render(view: '/404')
		}
		[algorithm: algorithmRequest]
	}

	def run(AlgorithmRequest algorithmRequest, String type) {
		try {
			AlgorithmType algorithmType = AlgorithmType.findByName(type)
			automatedService.runAlgorithm(algorithmRequest, algorithmType)
			render([ok: true] as JSON)
		} catch (AugurWorksException e) {
			log.warn e.getMessage()
			log.debug e.getStackTrace().join('\n      at ')
			render([ok: false, error: e.getMessage()] as JSON)
		} catch (e) {
			log.error e.getMessage()
			log.debug e.getStackTrace().join('\n      at ')
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	def resultCard(AlgorithmResult algorithmResult) {
		render(template: '/layouts/resultCard', model: [result: algorithmResult])
	}

	def create(AlgorithmRequest algorithmRequest) {
		[algorithmRequest: algorithmRequest]
	}

	def submitRequest(String name, int startOffset, int endOffset, String unit, String cronExpression, Long id, boolean overwrite) {
		try {
			Collection<String> cronAlgorithms = JSON.parse(params.cronAlgorithms)
			Collection<Map> dataSets = JSON.parse(params.dataSets)
			if (overwrite && id) {
				AlgorithmRequest deleteRequest = AlgorithmRequest.get(id)
				autoKickoffService.clearJob(deleteRequest)
				deleteRequest?.delete(flush: true)
			}
			AlgorithmRequest algorithmRequest = constructAlgorithmRequest(name, startOffset, endOffset, Unit[unit], cronExpression, dataSets, cronAlgorithms)
			algorithmRequest.save()
			if (algorithmRequest.hasErrors()) {
				throw new AugurWorksException('The request could not be created, please check that the name is unique.')
			}
			algorithmRequest.updateDataSets(dataSets)
			if (algorithmRequest.cronExpression) {
				autoKickoffService.scheduleKickoffJob(algorithmRequest)
			}
			render([ok: true, id: algorithmRequest.id] as JSON)
		} catch (e) {
			log.error e.getMessage()
			log.debug e.getStackTrace().join('\n      at ')
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	def checkRequest(int startOffset, int endOffset, String unit, String cronExpression) {
		try {
			if (cronExpression && !CronExpression.isValidExpression(cronExpression)) {
				throw new AugurWorksException('Invalid Cron Expression')
			}
			Collection<Map> dataSets = JSON.parse(params.dataSets)
			AlgorithmRequest algorithmRequest = constructAlgorithmRequest(null, startOffset, endOffset, Unit[unit], cronExpression, dataSets, [])
			algorithmRequest.updateDataSets(dataSets, false)
			SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest)
			dataRetrievalService.smartSpline(splineRequest)
			render([ok: true] as JSON)
		} catch (e) {
			log.warn e.getMessage()
			log.debug e.getStackTrace().join('\n      at ')
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	private AlgorithmRequest constructAlgorithmRequest(String name, int startOffset, int endOffset, Unit unit, String cronExpression, Collection<Map> dataSets, Collection<String> cronAlgorithms) {
		Map dependantDataSetMap = dataSets.grep { it.dependant }.first()
		Map parameters = [
			name: name,
			startOffset: startOffset,
			endOffset: endOffset,
			unit: unit,
			cronExpression: cronExpression,
			cronAlgorithms: cronAlgorithms.collect { AlgorithmType.findByName(it) },
			dependantSymbol: dependantDataSetMap.symbol
		]
		return new AlgorithmRequest(parameters)
	}

	def deleteRequest(AlgorithmRequest algorithmRequest) {
		try {
			autoKickoffService.clearJob(algorithmRequest)
			algorithmRequest.delete(flush: true)
			render([ok: true] as JSON)
		} catch(e) {
			log.error e.getMessage()
			log.debug e.getStackTrace().join('\n      at ')
			render([ok: false] as JSON)
		}
	}

	def deleteResult(AlgorithmResult algorithmResult) {
		try {
			algorithmResult.delete(flush: true)
			render([ok: true] as JSON)
		} catch(e) {
			log.error e.getMessage()
			log.debug e.getStackTrace().join('\n      at ')
			render([ok: false] as JSON)
		}
	}

	def searchSymbol(String keyword) {
		render([
			success: true,
			results: keyword.size() > 1 ? dataRetrievalService.searchSymbol(keyword)*.toResultMap() : []
		] as JSON)
	}
}
