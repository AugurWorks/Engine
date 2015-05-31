package com.augurworks.engine

import grails.converters.JSON

class AlgorithmRequestController {

	static scaffold = true

	def index() {
		[requests: AlgorithmRequest.list()]
	}

	def create() {
		[dataSets: DataSet.list()*.name]
	}

	def submitRequest() {
		Date startDate = Date.parse('yyyy-MM-dd', params.startDate);
		Date endDate = Date.parse('yyyy-MM-dd', params.endDate);
		Collection<Map> dataSets = JSON.parse(params.dataSets);
		AlgorithmRequest algorithmRequest = new AlgorithmRequest(startDate: startDate, endDate: endDate).save();
		dataSets.each { Map dataSet ->
			algorithmRequest.addToRequestDataSets(
				dataSet: DataSet.findByName(dataSet.name),
				offset: dataSet.offset
			);
		}
		render([success: true] as JSON)
	}
}
