package com.augurworks.engine.taglibs

class AugurworksTagLib {

	def springSecurityService

	static namespace = 'aw'

	/**
	 * Renders the user's avatar image.
	 */
	def avatar = { attributes ->
		out << '<img class="' + attributes['class'] + '" style="' + attributes['style'] + '" src="' + springSecurityService.currentUser.avatarUrl + '" />'
	}
}
