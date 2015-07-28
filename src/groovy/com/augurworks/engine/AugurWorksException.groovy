package com.augurworks.engine

class AugurWorksException extends RuntimeException {

	String message

	AugurWorksException(String message) {
		this.message = message
	}

	String getMessage() {
		return this.message
	}
}
