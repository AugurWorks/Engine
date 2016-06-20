package com.augurworks.engine.data

import com.augurworks.engine.domains.AlgorithmRequest

class SplineRequest {

	AlgorithmRequest algorithmRequest
	boolean prediction = false
	boolean includeDependent = true
	Date now = new Date()

	Date getStartDate() {
		return algorithmRequest.getStartDate(this.now)
	}

	Date getEndDate() {
		return algorithmRequest.getEndDate(this.now)
	}
}
