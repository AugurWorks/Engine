package com.augurworks.engine.services

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import spock.lang.Specification

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.Global

@Build([AlgorithmResult])
@Mock([AlgorithmResult])
@TestFor(AlfredService)
class AlfredServiceSpec extends Specification {

	void "test check incomplete algorithms"() {
		given:
		int counter = 0
		service.metaClass.checkAlgorithm = { AlgorithmResult result ->
			counter++
		}
		AlgorithmResult.build(complete: false, modelType: Global.MODEL_TYPES[0])
		AlgorithmResult.build(complete: false, modelType: Global.MODEL_TYPES[1])
		AlgorithmResult.build(complete: true, modelType: Global.MODEL_TYPES[0])
		AlgorithmResult.build(complete: true, modelType: Global.MODEL_TYPES[1])

		when:
		service.checkIncompleteAlgorithms()

		then:
		counter == 1
	}
}
