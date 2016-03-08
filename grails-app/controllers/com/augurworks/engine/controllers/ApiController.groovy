package com.augurworks.engine.controllers

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.AugurWorksException
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.helper.Global
import com.augurworks.engine.services.ApiService
import com.augurworks.engine.services.AutomatedService
import com.augurworks.engine.slack.Attachment
import com.augurworks.engine.slack.SlashMessage

class ApiController {

	GrailsApplication grailsApplication
	ApiService apiService
	AutomatedService automatedService

	def slack(String token, String user_name, String text, String response_url) {
		SlashMessage slashMessage = new SlashMessage()
		try {
			if (token != grailsApplication.config.slack.slash.token) {
				throw new RuntimeException('Invalid token')
			}
			Collection<String> arguments = text.toLowerCase().replace('run ', '').replace('for ', '').split(' ')
			if (arguments.size() == 0) {
				throw new AugurWorksException('No commands specified')
			}
			int requestCount = 1
			if (arguments.first().matches('^\\d+$')) {
				requestCount = Integer.parseInt(arguments.first())
				arguments.remove(0)
			}
			String command = arguments.first()
			arguments.remove(0)
			switch (command) {
				case 'list':
					String message = apiService.getListMessage()
					slashMessage.withText('Algorithm Request List').withAttachment(new Attachment(message))
					break
				case 'running':
					String message = apiService.getRunningMessage()
					slashMessage.withText('Running Request List').withAttachment(new Attachment(message))
					break
				case 'recent':
					int numberOfRuns = (arguments.size() > 0 && arguments.first().matches('^\\d+$')) ? Integer.parseInt(arguments.first()) : 5
					slashMessage = apiService.getRecentSlashMessage(slashMessage, numberOfRuns)
					slashMessage.withText('Recent Run List')
					break
				case 'ml': case 'mls': case 'alfred': case 'alfreds':
					if (command[-1] == 's') {
						command = command[0..-2]
					}
					if (arguments.first().matches('^\\d+$')) {
						requestCount = Integer.parseInt(arguments.first())
						arguments.remove(0)
					}
					String requestName = arguments.join(' ')
					AlgorithmRequest algorithmRequest = AlgorithmRequest.findByNameIlike(requestName)
					if (algorithmRequest) {
						String runType = Global.MODEL_TYPES[Global.SLASH_MAP[command]]
						runAsync {
							apiService.runRequest(response_url, requestName, user_name, runType, requestCount)
						}
						slashMessage.withText('Kicking off ' + (requestCount == 1 ? 'a(n)' : requestCount) + ' ' + runType + ' run(s) for ' + algorithmRequest.name + '...')
					} else {
						slashMessage.withText('No request found with the name ' + algorithmRequest.name)
					}
					break
				default:
					String message = apiService.getHelpMessage()
					slashMessage.withText('Engine Help').withAttachment(new Attachment(message))
					break
			}
			render(contentType: 'application/json') {
				slashMessage.toJson()
			}
		} catch (AugurWorksException e) {
			slashMessage.withText(e.getMessage())
			render(contentType: 'application/json') {
				slashMessage.toJson()
			}
		} catch (e) {
			log.warn(e)
			render(status: 500)
		}
	}
}
