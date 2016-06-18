package com.augurworks.engine.messaging

import java.io.Serializable

public class TrainingMessage implements Serializable {

	TrainingMessage() { }

	TrainingMessage(String netId, String data) {
		this.netId = netId
		this.data = data
	}

	private final String netId
	private final String data

	public String getNetId() {
		return this.netId
	}

	public String getData() {
		return this.data
	}
}
