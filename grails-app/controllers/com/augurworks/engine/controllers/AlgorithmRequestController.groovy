package com.augurworks.engine.controllers

import grails.converters.JSON

import com.augurworks.engine.domains.AlgorithmRequest
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
		machineLearningService.createAlgorithm(algorithmRequest)
		render([ok: true] as JSON)
	}

	def create(AlgorithmRequest algorithmRequest) {
		[dataSets: DataSet.list()*.toString(), algorithmRequest: algorithmRequest]
	}

	def submitRequest(String startDate, String endDate) {
		Collection<Map> dataSets = JSON.parse(params.dataSets)
		Map dependantDataSetMap = dataSets.grep { it.dependant }.first()
		Map parameters = [
			startDate: Date.parse(Global.FORM_DATE_FORMAT, startDate),
			endDate: Date.parse(Global.FORM_DATE_FORMAT, endDate),
			dependantDataSet: DataSet.findByTicker(dependantDataSetMap.name.split(' - ')[0])
		]
		AlgorithmRequest algorithmRequest = new AlgorithmRequest(parameters).save()
		algorithmRequest.updateDataSets(dataSets)
		render([success: true, id: algorithmRequest.id] as JSON)
	}

	def deleteRequest(AlgorithmRequest algorithmRequest) {
		try {
			algorithmRequest.delete(flush: true)
			render([success: true] as JSON)
		} catch(e) {
			log.error e.message
			render([success: false] as JSON)
		}
	}
}
