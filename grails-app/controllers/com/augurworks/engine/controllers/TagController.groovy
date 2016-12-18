package com.augurworks.engine.controllers

import com.augurworks.engine.domains.AlgorithmRequest

class TagController {

    def show(String tag) {
        List<AlgorithmRequest> algorithmRequests = AlgorithmRequest.createCriteria().list {
            tags {
                inList('name', [tag])
            }
        }
        [tag: tag, requests: algorithmRequests]
    }
}
