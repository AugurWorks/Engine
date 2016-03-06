package com.augurworks.engine.helper

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders

class SlashMessage {

	String text
	String title
	String message
	String color

	SlashMessage(String text) {
		this.text = text
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
