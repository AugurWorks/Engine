package com.augurworks.engine.controllers

import com.augurworks.engine.domains.ProductResult
import grails.converters.JSON

class ProductResultController {

	def action(ProductResult productResult) {
		render([action: productResult.action] as JSON)
	}
}
