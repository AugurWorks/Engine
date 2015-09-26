package com.augurworks.engine.controllers

import grails.converters.JSON

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.helper.Global

class AlgorithmRequestController {

	def machineLearningService

	def index() {
		[requests: AlgorithmRequest.list()]
	}

	def show(AlgorithmRequest algorithmRequest) {
		[algorithm: algorithmRequest]
	}

	def run(AlgorithmRequest algorithmRequest) {
		try {
			machineLearningService.createAlgorithm(algorithmRequest)
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

	def submitRequest(int startOffset, int endOffset) {
		try {
			Collection<Map> dataSets = JSON.parse(params.dataSets)
			Map dependantDataSetMap = dataSets.grep { it.dependant }.first()
			Map parameters = [
				startOffset: startOffset,
				endOffset: endOffset,
				dependantDataSet: DataSet.findByTicker(dependantDataSetMap.name.split(' - ')[0])
			]
			AlgorithmRequest algorithmRequest = new AlgorithmRequest(parameters).save()
			algorithmRequest.updateDataSets(dataSets)
			render([ok: true, id: algorithmRequest.id] as JSON)
		} catch (e) {
			log.error e.getMessage()
			render([ok: false, error: e.getMessage()] as JSON)
		}
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
