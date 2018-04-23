package com.augurworks.engine.model.prediction

enum RuleEvaluationAction {
    BUY('#4DBD33'),
    SELL('#ff4444'),
    HOLD('#444444')

    public String color

    RuleEvaluationAction(String color) {
        this.color = color
    }
}
