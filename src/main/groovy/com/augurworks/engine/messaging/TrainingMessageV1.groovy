package com.augurworks.engine.messaging

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.helper.Global

public class TrainingMessageV1 extends TrainingMessage {

    TrainingMessageV1() {
        super()
    }

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

    List<PredictedValue> processResults(AlgorithmResult algorithmResult) {
        Collection<String> lines = data.split('\n')
        return lines[0..(lines.size() - 1)].each { String line ->
            Collection<String> cols = line.split(' ')
            return new PredictedValue(
                    date: Date.parse(Global.ALFRED_DATE_FORMAT, cols[0]),
                    value: cols[2].toDouble(),
                    algorithmResult: algorithmResult
            ).save()
        }
    }
}
