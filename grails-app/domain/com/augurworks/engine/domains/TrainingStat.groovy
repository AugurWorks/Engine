package com.augurworks.engine.domains

import com.augurworks.engine.helper.TrainingStopReason
import com.augurworks.engine.stats.TrainingStage

class TrainingStat {

    String netId
    Integer dataSets
    Integer rowCount
    Double learningConstant
    Integer secondsElapsed
    Integer roundsTrained
    Double rmsError
    TrainingStopReason trainingStopReason
    TrainingStage trainingStage

    Date dateCreated = new Date()

    static constraints = {
        trainingStopReason nullable: true
    }
}