package com.augurworks.engine.controllers

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.Global
import com.augurworks.engine.helper.SlashMessage
import com.augurworks.engine.services.AutomatedService

class ApiController {

	GrailsApplication grailsApplication
	AutomatedService automatedService

	def slack(String token, String user_name, String text, String response_url) {
		try {
			if (token != grailsApplication.config.slack.slash.token) {
				throw new RuntimeException('Invalid token')
			}
			Collection<String> commands = text.toLowerCase().replace('run ', '').replace('for ', '').split(' ')
			if (commands.size() == 0) {
				throw new AugurWorksException('No commands specified')
			}
			String serverUrl = grailsApplication.config.grails.serverURL
			SlashMessage slashMessage = new SlashMessage()
			switch (commands.first()) {
				case 'list':
					String message = AlgorithmRequest.list(sort: 'name').collect { AlgorithmRequest algorithmRequest ->
						Collection<AlgorithmResult> results = algorithmRequest.algorithmResults
						return [
							algorithmRequest.name + ': ',
							results.size() + ' runs, ',
							algorithmRequest.requestDataSets.size() + ' data sets, ',
							'Last run: ' + (results.size() > 0 ? results*.dateCreated.sort().first().format('MM/dd/yy HH:mm') : 'never') + ' ',
							'(<' + serverUrl + '/algorithmRequest/show/' + algorithmRequest.id + '|View>)'
						].join('')
					}.join('\n')
					slashMessage.withText('Algorithm Request List').withMessage(message)
					break
				case 'running':
					String message = AlgorithmResult.findAllByComplete(false, [sort: 'dateCreated']).collect { AlgorithmResult algorithmResult ->
						return [
							algorithmResult.modelType + ' run of ',
							algorithmResult.algorithmRequest.name + ' started at ',
							algorithmResult.dateCreated.sort().first().format('MM/dd/yy HH:mm') + ' ',
							'(<' + serverUrl + '/algorithmRequest/show/' + algorithmResult.algorithmRequest.id + '|View>)'
						].join('')
					}.join('\n') ?: 'No currently running requests'
					slashMessage.withText('Running Request List').withMessage(message)
					break
				case 'ml':
				case 'alfred':
					String requestName = commands.tail().join(' ')
					AlgorithmRequest algorithmRequest = AlgorithmRequest.findByNameIlike(requestName)
					if (algorithmRequest) {
						String runType = Global.MODEL_TYPES[Global.SLASH_MAP[commands.first()]]
						runAsync {
							SlashMessage defered = new SlashMessage().withUrl(response_url)
							try {
								automatedService.runAlgorithm(AlgorithmRequest.findByNameIlike(requestName), runType)
								defered.withText('@' + user_name + ' kicked off a(n) ' + runType + ' run for ' + requestName).isInChannel()
							} catch (AugurWorksException e) {
								defered.withText('Error: ' + e.getMessage())
							} catch (e) {
								log.error e
								log.info e.getStackTrace().join('\n')
								defered.withText('An error has occured, please validate the request in the Engine application')
							}
							defered.post()
						}
						slashMessage.withText('Kicking off ' + runType + ' for ' + algorithmRequest.name + '...')
					} else {
						slashMessage.withText('No request found with the name ' + algorithmRequest.name)
					}
					break
				default:
					String message = [
						'help - This help message',
						'list - List all existing requests and basic information about them',
						'running - List all running requests',
						'(run) alfred (for) [request name] - Kick off an Alfred run for a given request',
						'(run) ml (for) [request name] - Kick off a Machine Learning run for a given request'
					].join('\n')
					slashMessage.withText('Engine Help').withMessage(message)
					break
			}
			render(contentType: 'application/json') {
				slashMessage.toJson()
			}
		} catch (AugurWorksException e) {
			render(status: 500)
		} catch (e) {
			log.warn(e)
			render(status: 500)
		}
	}
}
