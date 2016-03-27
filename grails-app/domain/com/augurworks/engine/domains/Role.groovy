package com.augurworks.engine.domains

class Role {

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority unique: true
	}
}
