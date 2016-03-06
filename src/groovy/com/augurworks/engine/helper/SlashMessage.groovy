package com.augurworks.engine.helper

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders

class SlashMessage {

	String text
	boolean inChannel = false
	String title
	String message
	String color

	SlashMessage withText(String text) {
		this.text = text
		return this
	}

	SlashMessage isInChannel() {
		this.inChannel = true
		return this
	}

	SlashMessage withTitle(String title) {
		this.title = title
		return this
	}

	SlashMessage withMessage(String message) {
		this.message = message
		return this
	}

	SlashMessage withColor(String color) {
		this.color = color
		return this
	}

	Map toJson() {
		Map map = [
			text: this.text,
			response_type: this.inChannel ? 'in_channel' : 'ephemeral'
		]
		if (this.message) {
			map.attachments = [
				[
					title: this.title,
					text: this.message,
					color: this.color
				]
			]
		}
		return map
	}
}
