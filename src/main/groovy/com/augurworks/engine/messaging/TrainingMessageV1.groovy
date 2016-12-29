package com.augurworks.engine.messaging

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.Global
import com.augurworks.engine.model.RequestValueSet

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

    public static TrainingMessageV1 constructTrainingMessage(String id, AlgorithmRequest algorithmRequest, List<RequestValueSet> dataSets) {
        int rowNumber = dataSets*.values*.size().max()
        /*if (dataSets.first().values.size() != rowNumber - 1) {
         throw new AugurWorksException('Dependant data set not sized correctly compared to independent data sets')
         }*/
        Collection<String> lines = ['net ' + (dataSets.size() - 1) + ',5', 'train 1,700,0.1,700,0.000001', 'TITLES ' + dataSets.tail()*.name.join(',')
        ] + (0..(rowNumber - 1)).collect { int row ->
            // TO-DO: Will not work for predictions of more than one period
            Date date = dataSets*.values.first()[row]?.date ?: Common.calculatePredictionDate(algorithmRequest.unit, dataSets*.values.first()[row - 1].date, 1)
            return date.format(Global.ALFRED_DATE_FORMAT) + ' ' + (dataSets.first().values[row]?.value ?: 'NULL') + ' ' + dataSets.tail()*.values.collect {
                it[row].value
            }.join(',')
        }
        return new TrainingMessageV1(id).withData(lines.join('\n'))
    }
}