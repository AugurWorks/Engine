package com.augurworks.engine.helper;

public enum TrainingStopReason {
    OUT_OF_TIME("Ran out of time"),
    HIT_PERFORMANCE_CUTOFF("Hit performance cutoff"),
    HIT_TRAINING_LIMIT("Round limit reached"),
    BROKE_LOCAL_MAX("Broke at local maximum"),
    ;

    private String explanation;

    TrainingStopReason(String explanation) {
        this.explanation = explanation;
    }

    public String getExplanation() {
        return explanation;
    }
}
