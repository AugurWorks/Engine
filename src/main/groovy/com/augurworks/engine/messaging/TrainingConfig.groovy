package com.augurworks.engine.messaging

public class TrainingConfig implements Serializable {

    private Integer depth
    private Integer iterations
    private Double learningRate
    private Double maxBound
    private Double minBound
    private Double threshold
    private List<String> titles

    TrainingConfig withDepth(Integer depth) {
        this.depth = depth
        return this
    }

    TrainingConfig withIterations(Integer iterations) {
        this.iterations = iterations
        return this
    }

    TrainingConfig withLearningRate(Double learningRate) {
        this.learningRate = learningRate
        return this
    }

    TrainingConfig withMaxBound(Double maxBound) {
        this.maxBound = maxBound
        return this
    }

    TrainingConfig withMinBound(Double minBound) {
        this.minBound = minBound
        return this
    }

    TrainingConfig withThreshold(Double threshold) {
        this.threshold = threshold
        return this
    }

    TrainingConfig withTitles(List<String> titles) {
        this.titles = titles
        return this
    }

    Integer getDepth() {
        return this.depth
    }

    Integer getIterations() {
        return this.iterations
    }

    Double getLearningRate() {
        return this.learningRate
    }

    Double getMaxBound() {
        return this.maxBound
    }

    Double getMinBound() {
        return this.minBound
    }

    Double getThreshold() {
        return this.threshold
    }

    List<String> getTitles() {
        return this.titles
    }
}
