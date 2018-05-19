package com.augurworks.engine.model.prediction

class PredictionRuleResult {

    public String message
    public RuleEvaluationAction action
    public String actionMessage

    private PredictionRuleResult(String message) {
        this(message, null)
    }

    private PredictionRuleResult(String message, RuleEvaluationAction action) {
        this.message = message
        this.action = action
    }
}
