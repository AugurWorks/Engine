package com.augurworks.engine.services

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.transaction.Transactional

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.helper.SplineRequest

@Transactional
class AlfredService {

	GrailsApplication grailsApplication
	DataRetrievalService dataRetrievalService
	AutomatedService automatedService

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String postBody = constructPostBody(algorithmRequest)
		String trainingId = submitTraining(postBody)
		AlgorithmResult algorithmResult = new AlgorithmResult([
			algorithmRequest: algorithmRequest,
			startDate: algorithmRequest.startDate,
			endDate: algorithmRequest.endDate,
			alfredModelId: trainingId,
			modelType: AlgorithmType.ALFRED
		])
		algorithmResult.save()
	}

	String constructPostBody(AlgorithmRequest algorithmRequest) {
		SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest, prediction: true)
		Collection<RequestValueSet> dataSets = dataRetrievalService.smartSpline(splineRequest).sort { RequestValueSet requestValueSetA, RequestValueSet requestValueSetB ->
			return (requestValueSetB.name == algorithmRequest.dependantDataSet.ticker) <=> (requestValueSetA.name == algorithmRequest.dependantDataSet.ticker) ?: requestValueSetA.name <=> requestValueSetB.name
		}
		int rowNumber = dataSets*.values*.size().max()
		if (!automatedService.areDataSetsCorrectlySized(dataSets.tail(), rowNumber)) {
			throw new AugurWorksException('Request datasets aren\'t all the same length')
		}
		/*if (dataSets.first().values.size() != rowNumber - 1) {
			throw new AugurWorksException('Dependant data set not sized correctly compared to independant data sets')
		}*/
		Collection<String> lines = [
			'net ' + (dataSets.size() - 1) + ',5',
			'train 1,2500,0.1,2500,0.01',
			'TITLES ' + dataSets.tail()*.name.join(',')
		] + (0..(rowNumber - 1)).collect { int row ->
			// TO-DO: Will not work for predictions of more than one period
			Date date = dataSets*.values.first()[row]?.date ?: Common.calculatePredictionDate(algorithmRequest.unit, dataSets*.values.first()[row - 1].date, 1)
			return date.format(Global.ALFRED_DATE_FORMAT) + ' ' + (dataSets.first().values[row]?.value ?: 'NULL') + ' ' + dataSets.tail()*.values.collect { it[row].value }.join(',')
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
		}
		throw new AugurWorksException('Alfred was not able to process the submitted request')
	}

	void checkIncompleteAlgorithms() {
		Collection<AlgorithmResult> algorithmResults = AlgorithmResult.findAllByCompleteAndModelType(false, AlgorithmType.ALFRED)
		algorithmResults.each { AlgorithmResult algorithmResult ->
			try {
				checkAlgorithm(algorithmResult)
			} catch (AugurWorksException e) {
				log.warn 'Algorithm Result ' + algorithmResult.id + ' had an error: ' + e.getMessage()
				log.debug e.getStackTrace().join('\n      at ')
			} catch (e) {
				log.error e.getMessage()
				log.debug e.getStackTrace().join('\n      at ')
			}
		}
	}

	void checkAlgorithm(AlgorithmResult algorithmResult) {
		String url = grailsApplication.config.alfred.url
		RestResponse resp = new RestBuilder().get(url + '/result/' + algorithmResult.alfredModelId)
		if (resp.status == 200 && resp.text != 'IN_PROGRESS') {
			algorithmResult.complete = true
			if (resp.text != 'UNKNOWN') {
				processResponse(algorithmResult, resp.text)
			}
			algorithmResult.save()
			automatedService.postProcessing(algorithmResult)
		} else if (resp.status == 500) {
			throw new AugurWorksException('Alfred was not able to process the submitted request')
		}
	}

	void processResponse(AlgorithmResult algorithmResult, String text) {
		Collection<String> lines = text.split('\n')
		lines[4..(lines.size() - 1)].each { String line ->
			Collection<String> cols = line.split(' ')
			new PredictedValue(
				date: Date.parse(Global.ALFRED_DATE_FORMAT, cols[0]),
				value: cols[2].toDouble(),
				algorithmResult: algorithmResult
			).save()
		}
	}
}
