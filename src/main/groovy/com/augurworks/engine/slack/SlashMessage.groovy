package com.augurworks.engine.slack

import grails.plugins.rest.client.RestBuilder

class SlashMessage {

	String text
	boolean inChannel = false
	String url
	Collection<Attachment> attachments = []

	SlashMessage withText(String text) {
		this.text = text
		return this
	}

	SlashMessage isInChannel() {
		this.inChannel = true
		return this
	}

	SlashMessage withUrl(String url) {
		this.url = url
		return this
	}

	SlashMessage withAttachment(Attachment attachment) {
		this.attachments.push(attachment)
		return this
	}

	Map toJson() {
		return [
			text: this.text,
			response_type: this.inChannel ? 'in_channel' : 'ephemeral',
			attachments: this.attachments
		]
	}

	void post() {
		new RestBuilder().post(this.url) {
			json this.toJson()
		}
	}
}
