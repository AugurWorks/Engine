package com.augurworks.engine.services

import grails.transaction.Transactional

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.slack.Attachment
import com.augurworks.engine.slack.SlashMessage

@Transactional
class ApiService {

	GrailsApplication grailsApplication
	AutomatedService automatedService

	static final String DATE_FORMAT = 'MM/dd/yy HH:mm'

	String getListMessage() {
		String serverUrl = grailsApplication.config.grails.serverURL
		return AlgorithmRequest.list(sort: 'name').collect { AlgorithmRequest algorithmRequest ->
			Collection<AlgorithmResult> results = algorithmRequest.algorithmResults
			return [
				algorithmRequest.name + ': ',
				results.size() + ' runs, ',
				algorithmRequest.requestDataSets.size() + ' data sets, ',
				'Last run: ' + (results.size() > 0 ? results*.dateCreated.sort().first().format(DATE_FORMAT) : 'never') + ' ',
				'(<' + serverUrl + '/algorithmRequest/show/' + algorithmRequest.id + '|View>)'
			].join('')
		}.join('\n')
	}

	String getRunningMessage() {
		String serverUrl = grailsApplication.config.grails.serverURL
		return AlgorithmResult.findAllByComplete(false, [sort: 'dateCreated']).collect { AlgorithmResult algorithmResult ->
			return [
				algorithmResult.modelType + ' run of ',
				algorithmResult.algorithmRequest.name + ' started at ',
				algorithmResult.dateCreated.format(DATE_FORMAT) + ' ',
				'(<' + serverUrl + '/algorithmRequest/show/' + algorithmResult.algorithmRequest.id + '|View>)'
			].join('')
		}.join('\n') ?: 'No currently running requests'
	}

	SlashMessage getRecentSlashMessage(SlashMessage slashMessage, int numberOfRuns) {
		AlgorithmResult.findAllByComplete(true, [sort: 'dateCreated', order: 'desc', max: numberOfRuns]).each { AlgorithmResult algorithmResult ->
			PredictedValue predictedValue = algorithmResult.futureValue
			if (predictedValue) {
				Map slackMap = predictedValue.slackMap
				slashMessage.withAttachment(new Attachment(slackMap.message).withTitle(slackMap.title + ' run at ' + algorithmResult.dateCreated.format(DATE_FORMAT)).withColor(slackMap.color))
			} else {
				slashMessage.withAttachment(new Attachment('No predictions').withTitle(algorithmResult.algorithmRequest.name))
			}
		}
		return slashMessage
	}

	void runRequest(String responseUrl, String requestName, String userName, String runType) {
		SlashMessage defered = new SlashMessage().withUrl(responseUrl)
		try {
			automatedService.runAlgorithm(AlgorithmRequest.findByNameIlike(requestName), runType)
			defered.withText('@' + userName + ' kicked off a(n) ' + runType + ' run for ' + requestName).isInChannel()
		} catch (AugurWorksException e) {
			defered.withText('Error: ' + e.getMessage())
		} catch (e) {
			log.error e
			log.info e.getStackTrace().join('\n')
			defered.withText('An error has occured, please validate the request in the Engine application')
		}
		defered.post()
	}

	String getHelpMessage() {
		return [
			'help - This help message',
			'list - List all existing requests and basic information about them',
			'running - List all running requests',
			'recent [number] - List a number (default 5) of recent run results',
			'(run) alfred (for) [request name] - Kick off an Alfred run for a given request',
			'(run) ml (for) [request name] - Kick off a Machine Learning run for a given request'
		].join('\n')
	}
}
