package com.augurworks.engine

class Role {

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority unique: true
	}
}
