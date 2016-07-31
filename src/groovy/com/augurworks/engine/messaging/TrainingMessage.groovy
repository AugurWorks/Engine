package com.augurworks.engine.messaging

import com.augurworks.engine.domains.TrainingStat

public class TrainingMessage implements Serializable {

	TrainingMessage() { }

	TrainingMessage(String netId, String data) {
		this.netId = netId
		this.data = data
	}

	private final String netId
	private final String data
	
	private Map<String, String> metadata
	private List<TrainingStat> trainingStats

	public String getNetId() {
		return this.netId
	}

	public String getData() {
		return this.data
	}

	public TrainingMessage withMetadata(Map<String, String> metadata) {
		this.metadata = metadata
		return this
	}

	public List<TrainingStat> getTrainingStats() {
		return this.trainingStats ?: []
	}

	public Map<String, String> getMetadata() {
		return this.metadata
	}
}
