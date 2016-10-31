package com.augurworks.engine.taglibs

class AugurworksTagLib {

	def springSecurityService

	static namespace = 'aw'

	/**
	 * Renders the user's avatar image.
	 */
	def avatar = { attrs ->
		out << '<img class="' + attrs['class'] + '" style="' + attrs['style'] + '" src="' + springSecurityService.currentUser.avatarUrl + '" />'
	}
}
