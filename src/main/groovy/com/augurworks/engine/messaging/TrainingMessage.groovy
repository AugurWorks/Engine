package com.augurworks.engine.messaging

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.domains.TrainingStat
import com.augurworks.engine.model.RequestValueSet

public abstract class TrainingMessage implements Serializable {

    TrainingMessage() { }

    TrainingMessage(String netId) {
        this.netId = netId
    }

    private String netId

    private Map<String, String> metadata
    private List<TrainingStat> trainingStats

    public String getNetId() {
        return this.netId
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

    public void setNetId(String netId) {
        this.netId = netId
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata
    }

    public void setTrainingStats(List<TrainingStat> trainingStats) {
        this.trainingStats = trainingStats
    }

    abstract List<PredictedValue> processResults(AlgorithmResult algorithmResult)

    abstract static TrainingMessage constructTrainingMessage(String id, AlgorithmRequest algorithmRequest, List<RequestValueSet> dataSets)
}
