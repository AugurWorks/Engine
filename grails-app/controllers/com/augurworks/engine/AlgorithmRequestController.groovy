package com.augurworks.engine

import com.augurworks.engine.helper.Global
import grails.converters.JSON

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
		redirect(action: 'show', id: algorithmRequest.id)
	}

	def create(AlgorithmRequest algorithmRequest) {
		[dataSets: DataSet.list()*.toString(), algorithmRequest: algorithmRequest]
	}

	def submitRequest() {
		Collection<Map> dataSets = JSON.parse(params.dataSets)
		Map dependantDataSetMap = dataSets.grep { it.dependant }.first()
		Map parameters = [
			startDate: Date.parse(Global.DATE_FORMAT, params.startDate),
			endDate: Date.parse(Global.DATE_FORMAT, params.endDate),
			dependantDataSet: DataSet.findByTicker(dependantDataSetMap.name.split(' - ')[0])
		]
		AlgorithmRequest algorithmRequest = AlgorithmRequest.get(params.id) ?: new AlgorithmRequest(parameters).save();
		if (params.id == algorithmRequest.id.toString()) {
			algorithmRequest.updateFields(parameters)
		}
		algorithmRequest.updateDataSets(dataSets)
		render([success: true, id: algorithmRequest.id] as JSON)
	}

	def deleteRequest() {
		AlgorithmRequest algorithmRequest = AlgorithmRequest.get(params.id);
		try {
			algorithmRequest.delete()
			render([success: true] as JSON)
		} catch(e) {
			log.error e.message
			render([success: false] as JSON)
		}
	}
}
