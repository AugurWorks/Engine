package com.augurworks.engine.messaging

public class TrainingMessageV1 extends TrainingMessage {

    TrainingMessageV1(String id) {
        super(id)
    }

    private String data

    public TrainingMessageV1 withData(String data) {
        this.data = data
        return this
    }

    public String getData() {
        return this.data
    }
}
