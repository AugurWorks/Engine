package com.augurworks.engine.services

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.RequestValueSet

@Transactional
class AlfredService {

	GrailsApplication grailsApplication
	DataRetrievalService dataRetrievalService
	MachineLearningService machineLearningService

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String postBody = constructPostBody(algorithmRequest)
		String trainingId = submitTraining(postBody)
		AlgorithmResult algorithmResult = new AlgorithmResult([
			algorithmRequest: algorithmRequest,
			startDate: algorithmRequest.startDate,
			endDate: algorithmRequest.endDate,
			alfredModelId: trainingId,
			modelType: Global.MODEL_TYPES[1]
		])
		algorithmResult.save()
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
			'net ' + (dataSets.size() - 1) + ',4',
			'train 1,1000,0.3,500,0.1',
			'TITLES ' + dataSets.tail()*.name.join(',')
		] + (0..(rowNumber - 1)).collect { int row ->
			return dataSets*.values.first()[row].date.format(Global.DATE_FORMAT) + ' ' + dataSets.first().values[row].value + ' ' + dataSets*.values.tail().collect { it[row].value }.join(',')
		}
		return lines.join('\n')
	}

	String submitTraining(String postBody) {
		String url = grailsApplication.config.alfred.url
		RestResponse resp = new RestBuilder().post(url + '/train') {
			body(postBody)
		}
		if (resp.status == 200) {
			return resp.text
		} else {
			throw new AugurWorksException('Alfred was not able to process the submitted request')
		}
	}
}
