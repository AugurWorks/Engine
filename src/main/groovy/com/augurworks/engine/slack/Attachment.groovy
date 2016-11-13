package com.augurworks.engine.slack

class Attachment {

	String title
	String text
	String color

	Attachment(String text) {
		this.text = text
	}

	Attachment withTitle(String title) {
		this.title = title
		return this
	}

	Attachment withColor(String color) {
		this.color = color
		return this
	}
}
