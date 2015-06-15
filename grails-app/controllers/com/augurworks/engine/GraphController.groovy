package com.augurworks.engine

import grails.converters.JSON

class GraphController {

	def dataRetrievalService

	def line(AlgorithmRequest algorithmRequest) {
		[algorithmRequest: algorithmRequest, requests: AlgorithmRequest.list()]
	}

	def getData(AlgorithmRequest algorithmRequest) {
		if (algorithmRequest) {
			Collection<Map> data = dataRetrievalService.getRequestValues(algorithmRequest);
			render([success: true, data: data] as JSON)
		} else {
			render([success: true, data: []] as JSON)
		}
	}
}
