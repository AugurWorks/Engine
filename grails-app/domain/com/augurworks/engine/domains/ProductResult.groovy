package com.augurworks.engine.domains

class ProductResult {

    Date adjustedDateCreated
    ProductResult previousRun
    AlgorithmResult realTimeResult
    AlgorithmResult closeResult

    static belongsTo = [product: Product]

    Product product

    static constraints = {
        previousRun nullable: true
        realTimeResult nullable: true
        closeResult nullable: true
    }

    boolean isTooVolatile() {
        return false
    }

    boolean isAllPositive() {
        return [realTimeResult, closeResult]*.actualValue.collect { it > 0 }.every()
    }

    boolean isAllNegative() {
        return [realTimeResult, closeResult]*.actualValue.collect { it < 0 }.every()
    }

    Double getRealTimeDiff() {
        return getDiff(realTimeResult.actualValue, previousRun?.realTimeResult?.actualValue)
    }

    Double getCloseDiff() {
        return getDiff(closeResult.actualValue, previousRun?.closeResult?.actualValue)
    }

    private Double getDiff(Double currentValue, Double previousValue) {
        if (!currentValue || !previousValue) {
            return null
        }
        return currentValue - previousValue
    }
}
