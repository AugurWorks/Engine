package com.augurworks.engine.controllers

import com.augurworks.engine.services.AutomatedService
import grails.converters.JSON

class AutomatedServiceController {

	AutomatedService automatedService

	def run(Integer id) {
		automatedService.runCronAlgorithms(id)
		render([ok: true] as JSON)
	}
}
