package com.augurworks.engine.domains

class RequestTag {

    String name

    static belongsTo = [algorithmRequest: AlgorithmRequest]

    AlgorithmRequest algorithmRequest
}
