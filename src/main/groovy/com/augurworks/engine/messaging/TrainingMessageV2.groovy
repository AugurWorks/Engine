package com.augurworks.engine.messaging

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.Global
import com.augurworks.engine.model.RequestValueSet

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

    public static TrainingMessageV2 constructTrainingMessage(String id, AlgorithmRequest algorithmRequest, List<RequestValueSet> dataSets) {
        int rowNumber = dataSets*.values*.size().max()
        TrainingConfig trainingConfig = new TrainingConfig()
                .withTitles(dataSets.tail()*.name)
                .withIterations(algorithmRequest.trainingRounds)
                .withLearningRate(algorithmRequest.learningConstant)
                .withDepth(algorithmRequest.depth)
        List<List<String>> data = (0..(rowNumber - 1)).collect { int rowNum ->
            // TO-DO: Will not work for predictions of more than one period
            Date date = dataSets*.values.first()[rowNum]?.date ?: Common.calculatePredictionDate(algorithmRequest.unit, dataSets*.values.first()[rowNum - 1].date, 1)
            List<String> row = new ArrayList<>()
            row.add(date.format(Global.ALFRED_DATE_FORMAT))
            row.add(dataSets.first().values[rowNum]?.value.toString() ?: 'NULL')
            row.addAll(dataSets.tail()*.values.collect { it[rowNum].value.toString() })
            return row
        }
        return new TrainingMessageV2(id).withTrainingConfig(trainingConfig).withData(data)
    }
}
