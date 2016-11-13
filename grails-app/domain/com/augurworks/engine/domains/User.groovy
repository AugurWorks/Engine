package com.augurworks.engine.domains

import java.security.SecureRandom

class User {

	transient springSecurityService

	String username
	String password = generator()
	String avatarUrl
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	static transients = ['springSecurityService']

	static constraints = {
		username unique: true
		avatarUrl nullable: true
	}

	static mapping = {
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this)*.role as Set
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password)
	}

	String generator(int len = 20) {
		SecureRandom rand = new SecureRandom()
		String alpha = (('A'..'Z') + ('a'..'z') + ('0'..'9')).join()
		return (1..len).collect { alpha[rand.nextInt(alpha.length())] }.join()
	}
}
