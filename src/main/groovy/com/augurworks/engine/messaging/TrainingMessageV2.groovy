package com.augurworks.engine.messaging

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.helper.Global

public class TrainingMessageV2 extends TrainingMessage {

    TrainingMessageV2() {
        super()
    }

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

    public void setTrainingConfig(TrainingConfig trainingConfig) {
        this.trainingConfig = trainingConfig
    }

    public void setData(List<List<String>> data) {
        this.data = data
    }

    List<PredictedValue> processResults(AlgorithmResult algorithmResult) {
        return data.collect { List<String> row ->
            return new PredictedValue(
                date: Date.parse(Global.ALFRED_DATE_FORMAT, row[0]),
                value: row.last().toDouble(),
                algorithmResult: algorithmResult
            ).save()
        }
    }
}
