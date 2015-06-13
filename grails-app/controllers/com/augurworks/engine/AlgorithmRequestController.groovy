package com.augurworks.engine

import grails.converters.JSON

class AlgorithmRequestController {

	static scaffold = true

	def index() {
		[requests: AlgorithmRequest.list()]
	}

	def create(AlgorithmRequest algorithmRequest) {
		[dataSets: DataSet.list()*.toString(), algorithmRequest: algorithmRequest]
	}

	def submitRequest() {
		Date startDate = Date.parse('yyyy-MM-dd', params.startDate);
		Date endDate = Date.parse('yyyy-MM-dd', params.endDate);
		Collection<Map> dataSets = JSON.parse(params.dataSets);
		AlgorithmRequest algorithmRequest = AlgorithmRequest.get(params.id) ?: new AlgorithmRequest(startDate: startDate, endDate: endDate).save();
		algorithmRequest.requestDataSets*.id.each { long requestDataSetId ->
			algorithmRequest.removeFromRequestDataSets(RequestDataSet.get(requestDataSetId));
		}
		dataSets.each { Map dataSet ->
			algorithmRequest.addToRequestDataSets(
				dataSet: DataSet.findByTicker(dataSet.name.split(' - ')[0]),
				offset: dataSet.offset
			);
		}
		render([success: true, id: algorithmRequest.id] as JSON)
	}
}
