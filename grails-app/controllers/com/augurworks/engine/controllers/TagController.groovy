package com.augurworks.engine.controllers

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.RequestTag

class TagController {

    def index() {
        List<Map> tags = RequestTag.list()*.name.unique().sort().collect { String tag -> [
            name: tag,
            requests: getMatchingAlgorithmRequests(tag)
        ]}
        [tags: tags]
    }

    def show(String tag) {
        List<AlgorithmRequest> algorithmRequests = getMatchingAlgorithmRequests(tag)
        [tag: tag, requests: algorithmRequests]
    }

    List<AlgorithmRequest> getMatchingAlgorithmRequests(String tag) {
        return AlgorithmRequest.createCriteria().list {
            tags {
                inList('name', [tag])
            }
        }
    }
}
