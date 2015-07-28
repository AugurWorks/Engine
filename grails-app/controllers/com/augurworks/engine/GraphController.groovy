package com.augurworks.engine

import com.augurworks.engine.helper.RequestValueSet
import grails.converters.JSON

class GraphController {

	def dataRetrievalService

	def line(AlgorithmRequest algorithmRequest) {
		[algorithmRequest: algorithmRequest, requests: AlgorithmRequest.list()]
	}

	def getData(AlgorithmRequest algorithmRequest) {
		if (algorithmRequest) {
			Collection<RequestValueSet> data = dataRetrievalService.smartSpline(algorithmRequest, false)
			render([success: true, data: data] as JSON)
		} else {
			render([success: true, data: []] as JSON)
		}
	}
}
