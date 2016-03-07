package com.augurworks.engine.slack

import grails.plugins.rest.client.RestBuilder
import grails.util.Holders

class SlackMessage {

	String message
	String channel
	String title
	String botName
	String color
	String link

	SlackMessage(String message, String channel) {
		this.message = message
		this.channel = channel
	}

	SlackMessage withTitle(String title) {
		this.title = title
		return this
	}

	SlackMessage withBotName(String botName) {
		this.botName = botName
		return this
	}

	SlackMessage withColor(String color) {
		this.color = color
		return this
	}

	SlackMessage withLink(String link) {
		this.link = link
		return this
	}

	void send() {
		Map config = Holders.config.grails.plugin.slacklogger
		String formattedMessage = this.link ? this.message + ' (<' + this.link + '|Open>)' : this.message
		Map field = [
			title: this.title,
			value: formattedMessage,
			short: false
		]
		new RestBuilder().post(config.webhook) {
			json {
				username = this.botName ?: 'AugurWorks Engine'
				fallback = formattedMessage
				color = this.color ?: '#666666'
				fields = [field]
				channel = this.channel
			}
		}
	}
}
