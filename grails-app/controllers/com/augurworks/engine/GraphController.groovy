package com.augurworks.engine

import grails.converters.JSON

import com.augurworks.engine.helper.RequestValueSet

class GraphController {

	def dataRetrievalService

	def line(AlgorithmRequest algorithmRequest) {
		[algorithmRequest: algorithmRequest, requests: AlgorithmRequest.list()]
	}

	def getData(AlgorithmRequest algorithmRequest) {
		if (algorithmRequest) {
			Collection<RequestValueSet> data = dataRetrievalService.smartSpline(algorithmRequest, false)
			render([success: true, data: data*.toMap()] as JSON)
		} else {
			render([success: true, data: []] as JSON)
		}
	}

	def getResultData(AlgorithmResult algorithmResult) {
		if (algorithmResult) {
			AlgorithmRequest algorithmRequest = algorithmResult.algorithmRequest
			Collection<Map> data = dataRetrievalService.smartSpline(algorithmRequest, false)*.toMap()
			String key = algorithmRequest.dependantDataSet.ticker + ' - Prediction'
			Map prediction = [
				name: key,
				offset: algorithmRequest.predictionOffset,
				values: algorithmResult.predictedValues*.toMap()
			]
			data.push(prediction)
			render([success: true, data: data] as JSON)
		} else {
			render([success: true, data: []] as JSON)
		}
	}
}
