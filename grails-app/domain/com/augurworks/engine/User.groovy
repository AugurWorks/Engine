package com.augurworks.engine

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
		username blank: false, unique: true
		password blank: false
		avatarUrl nullable: true
	}

	static mapping = {
		password column: '`password`'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
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
		Random rand = new Random();
		String alpha = (('A'..'Z') + ('a'..'z') + ('0'..'9')).join();
		return (1..len).collect { alpha[rand.nextInt(alpha.length())] }.join();
	}
}
