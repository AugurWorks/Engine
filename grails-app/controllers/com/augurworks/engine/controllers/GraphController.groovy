package com.augurworks.engine.controllers

import grails.converters.JSON

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.RequestValueSet

class GraphController {

	def dataRetrievalService

	def line(AlgorithmRequest algorithmRequest) {
		[algorithmRequest: algorithmRequest, requests: AlgorithmRequest.list()]
	}

	def getData(AlgorithmRequest algorithmRequest) {
		if (algorithmRequest) {
			try {
				Collection<RequestValueSet> data = dataRetrievalService.smartSpline(algorithmRequest, false, true)
				render([ok: true, data: data*.toMap()] as JSON)
			} catch (AugurWorksException e) {
				log.warn e.getMessage()
				log.info e.getStackTrace().join('\n')
				render([ok: false, error: e.getMessage()] as JSON)
			} catch (e) {
				log.error e.getMessage()
				log.info e.getStackTrace().join('\n')
				render([ok: false, error: e.getMessage()] as JSON)
			}
		} else {
			render([ok: true, data: []] as JSON)
		}
	}

	def getResultData(AlgorithmResult algorithmResult) {
		if (algorithmResult) {
			try {
				AlgorithmRequest algorithmRequest = algorithmResult.algorithmRequest
				Collection<Map> data = dataRetrievalService.smartSpline(algorithmRequest, false, true)*.toMap()
				String key = algorithmRequest.dependantDataSet.ticker + ' - Prediction'
				Map prediction = [
					name: key,
					offset: algorithmRequest.predictionOffset,
					values: algorithmResult.predictedValues*.toMap()
				]
				data.push(prediction)
				render([ok: true, data: data] as JSON)
			} catch (AugurWorksException e) {
				log.warn e.getMessage()
				log.info e.getStackTrace().join('\n')
				render([ok: false, error: e.getMessage()] as JSON)
			} catch (e) {
				log.error e.getMessage()
				log.info e.getStackTrace().join('\n')
				render([ok: false, error: e.getMessage()] as JSON)
			}
		} else {
			render([ok: true, data: []] as JSON)
		}
	}
}
