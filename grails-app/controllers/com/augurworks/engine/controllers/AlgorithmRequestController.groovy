package com.augurworks.engine.controllers

import grails.converters.JSON

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.SplineRequest
import com.augurworks.engine.services.AlfredService
import com.augurworks.engine.services.DataRetrievalService
import com.augurworks.engine.services.MachineLearningService

class AlgorithmRequestController {

	MachineLearningService machineLearningService
	AlfredService alfredService
	DataRetrievalService dataRetrievalService

	def index() {
		[requests: AlgorithmRequest.list()]
	}

	def show(AlgorithmRequest algorithmRequest) {
		[algorithm: algorithmRequest]
	}

	def run(AlgorithmRequest algorithmRequest, String type) {
		try {
			if (type == Global.MODEL_TYPES[0]) {
				machineLearningService.createAlgorithm(algorithmRequest)
			} else if (type == Global.MODEL_TYPES[1]) {
				alfredService.createAlgorithm(algorithmRequest)
			}
			render([ok: true] as JSON)
		} catch (AugurWorksException e) {
			log.warn e.getMessage()
			render([ok: false, error: e.getMessage()] as JSON)
		} catch (e) {
			log.error e.getMessage()
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	def resultCard(AlgorithmResult algorithmResult) {
		render(template: '/layouts/resultCard', model: [result: algorithmResult])
	}

	def create(AlgorithmRequest algorithmRequest) {
		[dataSets: DataSet.list()*.toString(), algorithmRequest: algorithmRequest]
	}

	def submitRequest(int startOffset, int endOffset, String unit) {
		try {
			Collection<Map> dataSets = JSON.parse(params.dataSets)
			AlgorithmRequest algorithmRequest = constructAlgorithmRequest(startOffset, endOffset, unit, dataSets).save()
			algorithmRequest.updateDataSets(dataSets)
			render([ok: true, id: algorithmRequest.id] as JSON)
		} catch (e) {
			log.error e.getMessage()
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	def checkRequest(int startOffset, int endOffset, String unit) {
		try {
			Collection<Map> dataSets = JSON.parse(params.dataSets)
			AlgorithmRequest algorithmRequest = constructAlgorithmRequest(startOffset, endOffset, unit, dataSets)
			algorithmRequest.updateDataSets(dataSets, false)
			SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest)
			dataRetrievalService.smartSpline(splineRequest)
			render([ok: true] as JSON)
		} catch (e) {
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	private AlgorithmRequest constructAlgorithmRequest(int startOffset, int endOffset, String unit, Collection<Map> dataSets) {
		Map dependantDataSetMap = dataSets.grep { it.dependant }.first()
		Map parameters = [
			startOffset: startOffset,
			endOffset: endOffset,
			unit: unit,
			dependantDataSet: DataSet.findByTicker(dependantDataSetMap.name.split(' - ')[0])
		]
		return new AlgorithmRequest(parameters)
	}

	def deleteRequest(AlgorithmRequest algorithmRequest) {
		try {
			algorithmRequest.delete(flush: true)
			render([ok: true] as JSON)
		} catch(e) {
			log.error e.message
			render([ok: false] as JSON)
		}
	}
}
