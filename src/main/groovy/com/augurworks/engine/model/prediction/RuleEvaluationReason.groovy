package com.augurworks.engine.model.prediction

enum RuleEvaluationReason {
    MISSING_PREVIOUS_RUN('The previous run is missing'),
    MISSING_DATA('This run has missing data'),
    TOO_VOLATILE('The period is too volatile'),
    CLOSE_CHANGE_THRESHOLD('The close change threshold has matched'),
    PREVIOUS_RUN_MATCHED('The previous run was all positive or negative'),
    ALL_SAME_DIRECTION('The current run is all positive or all negative'),
    NO_RULES_MATCHED('No rules matched')

    public String description

    RuleEvaluationReason(String description) {
        this.description = description
    }
}
