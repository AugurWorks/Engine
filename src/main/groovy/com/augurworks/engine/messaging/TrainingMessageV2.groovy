package com.augurworks.engine.messaging

public class TrainingMessageV2 extends TrainingMessage {

    TrainingMessageV2(String id) {
        super(id)
    }

    private TrainingConfig trainingConfig
    private List<List<String>> data

    public TrainingMessageV2 withTrainingConfig(TrainingConfig trainingConfig) {
        this.trainingConfig = trainingConfig
        return this
    }

    public TrainingMessageV2 withData(List<List<String>> data) {
        this.data = data
        return this
    }

    public TrainingConfig getTrainingConfig() {
        return this.trainingConfig
    }

    public List<List<String>> getData() {
        return this.data
    }
}
