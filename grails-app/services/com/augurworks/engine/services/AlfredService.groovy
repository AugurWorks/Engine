package com.augurworks.engine.services

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
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

	void checkIncompleteAlgorithms() {
		Collection<AlgorithmResult> algorithmResults = AlgorithmResult.findAllByCompleteAndModelType(false, Global.MODEL_TYPES[1])
		algorithmResults.each { AlgorithmResult algorithmResult ->
			checkAlgorithm(algorithmResult)
		}
	}

	void checkAlgorithm(AlgorithmResult algorithmResult) {
		String url = grailsApplication.config.alfred.url
		RestResponse resp = new RestBuilder().get(url + '/get/' + algorithmResult.alfredModelId)
		if (resp.status == 200 && resp.text != 'IN_PROGRESS') {
			algorithmResult.complete = true
			if (resp.text != 'UNKNOWN') {
				processResponse(algorithmResult, resp.text)
			}
			algorithmResult.save()
		} else if (resp.status == 500) {
			throw new AugurWorksException('Alfred was not able to process the submitted request')
		}
	}

	void processResponse(AlgorithmResult algorithmResult, String text) {
		String dateFormat = algorithmResult.algorithmRequest.unit == 'Day' ? Global.DATE_FORMAT : Global.DATE_TIME_FORMAT
		Collection<String> lines = text.split('\n')
		lines[4..(lines.size() - 1)].each { String line ->
			Collection<String> cols = line.split(' ')
			new PredictedValue(
				date: Date.parse(dateFormat, cols[0]),
				value: cols[2].toDouble(),
				algorithmResult: algorithmResult
			).save()
		}
	}
}
