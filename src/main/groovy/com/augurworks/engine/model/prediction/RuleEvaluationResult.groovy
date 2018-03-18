package com.augurworks.engine.model.prediction

class RuleEvaluationResult {

    RuleEvaluationResult(RuleEvaluationAction action, String message) {
        this.action = action
        this.message = message
    }

    private final RuleEvaluationAction action
    private final String message
}
