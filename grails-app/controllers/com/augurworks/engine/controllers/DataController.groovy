package com.augurworks.engine.controllers

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.services.DataRetrievalService
import grails.converters.JSON

class DataController {

	DataRetrievalService dataRetrievalService

	def index(SingleDataRequest singleDataRequest) {
		render(dataRetrievalService.getSingleRequestValues(singleDataRequest).getValues() as JSON)
	}
}
