package com.augurworks.engine.controllers

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.helper.SlashMessage

class ApiController {

	GrailsApplication grailsApplication

	def slack(String token, String user_name, String text, String response_url) {
		try {
			if (token != grailsApplication.config.slack.slash.token) {
				throw new RuntimeException('Invalid token')
			}
			Collection<String> commands = text.split(' ')
			if (commands.size() == 0) {
				throw new AugurWorksException('No commands specified')
			}
			String serverUrl = grailsApplication.config.grails.serverURL
			SlashMessage slashMessage = new SlashMessage()
			switch (commands.first().toLowerCase()) {
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
				default:
					String message = [
						'[help] - This help message',
						'[list] - List all existing requests and basic information about them'
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
