package com.augurworks.engine.services

import grails.buildtestdata.mixin.Build
import grails.test.mixin.*
import spock.lang.Specification

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.AlgorithmType

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
		AlgorithmResult.build(complete: false, modelType: AlgorithmType.MACHINE_LEARNING)
		AlgorithmResult.build(complete: false, modelType: AlgorithmType.ALFRED)
		AlgorithmResult.build(complete: true, modelType: AlgorithmType.MACHINE_LEARNING)
		AlgorithmResult.build(complete: true, modelType: AlgorithmType.ALFRED)

		when:
		service.checkIncompleteAlgorithms()

		then:
		counter == 1
	}
}
