package com.augurworks.engine.services

import grails.transaction.Transactional

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.RequestValueSet

@Transactional
class AlfredService {

	GrailsApplication grailsApplication
	DataRetrievalService dataRetrievalService
	MachineLearningService machineLearningService

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String postBody = constructPostBody(algorithmRequest)
	}

	String constructPostBody(AlgorithmRequest algorithmRequest) {
		Collection<RequestValueSet> dataSets = dataRetrievalService.smartSpline(algorithmRequest, true).sort { RequestValueSet requestValueSetA, RequestValueSet requestValueSetB ->
			return (requestValueSetB.name == algorithmRequest.dependantDataSet.ticker) <=> (requestValueSetA.name == algorithmRequest.dependantDataSet.ticker) ?: requestValueSetA.name <=> requestValueSetB.name
		}
		int rowNumber = dataSets*.values*.size().max()
		if (!machineLearningService.areDataSetsCorrectlySized(dataSets, rowNumber)) {
			throw new AugurWorksException('Request datasets aren\'t all the same length.')
		}
		Collection<String> lines = [
			'net ' + (rowNumber - 1) + ',4',
			'train 1,1000,0.3,500,0.1',
			'TITLES ' + dataSets*.name.join(',')
		] + (0..(rowNumber - 1)).collect { int row ->
			return dataSets*.values.first()[row].date.format(Global.DATE_FORMAT) + ' ' + dataSets*.values.collect { it[row].value }.join(',')
		}
		return lines.join('\n')
	}
}
