package com.augurworks.engine.controllers

import com.augurworks.engine.domains.Role
import com.augurworks.engine.domains.User
import com.augurworks.engine.domains.UserRole
import grails.converters.JSON
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

class GithubController {

	def oauthService
	def userDetailsService
	def userCache

	def index() {
		String sessionKey = oauthService.findSessionKeyForAccessToken('github')
		def githubToken =  session[sessionKey]
		Map json = JSON.parse(oauthService.getGithubResource(githubToken, 'https://api.github.com/user').getBody())
		User user = User.findByUsername(json.login)
		if (!user) {
			Role userRole = Role.findByAuthority('ROLE_USER')
			user = new User(username: json.login).save()
			new UserRole(user: user, role: userRole).save()
		}
		user.avatarUrl = json.avatar_url
		user.save(flush: true)
		authenticate(json.login)
		redirect(uri: '/')
	}

	private void authenticate(String username) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(username)
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities()))
		userCache.removeUserFromCache(username)
	}
}
