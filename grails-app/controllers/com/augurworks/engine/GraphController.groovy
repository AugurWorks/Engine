package com.augurworks.engine

import grails.converters.JSON

class GraphController {

	def dataRetrievalService

	def line(AlgorithmRequest algorithmRequest) {
		if (algorithmRequest) {
			[algorithmRequest: algorithmRequest]
		} else {
			render(view: '404')
		}
	}

	def getData(AlgorithmRequest algorithmRequest) {
		Collection<Map> data = dataRetrievalService.getRequestValues(algorithmRequest);
		render([success: true, data: data] as JSON)
	}
}
