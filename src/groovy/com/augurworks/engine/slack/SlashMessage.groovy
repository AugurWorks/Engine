package com.augurworks.engine.slack

import grails.plugins.rest.client.RestBuilder

class SlashMessage {

	String text
	boolean inChannel = false
	String title
	String message
	String color
	String url

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

	SlashMessage withUrl(String url) {
		this.url = url
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

	void post() {
		new RestBuilder().post(this.url) {
			json this.toJson()
		}
	}
}
