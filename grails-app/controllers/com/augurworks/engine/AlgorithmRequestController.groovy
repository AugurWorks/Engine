package com.augurworks.engine

class AlgorithmRequestController {

	static scaffold = true

	def index() {
		[requests: AlgorithmRequest.list()]
	}
}
