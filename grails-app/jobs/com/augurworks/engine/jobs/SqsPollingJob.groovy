package com.augurworks.engine.jobs

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.augurworks.engine.messaging.TrainingMessage
import com.augurworks.engine.services.AlfredService
import com.fasterxml.jackson.databind.ObjectMapper

class SqsPollingJob {

	GrailsApplication grailsApplication
	AlfredService alfredService

	private final ObjectMapper mapper = new ObjectMapper()
	
	AmazonSQSClient sqsClient = new AmazonSQSClient()

	static triggers = {
		simple repeatInterval: 20000
	}

	void execute() {
		String queueName = grailsApplication.config.messaging.sqsName
		if (grailsApplication.config.messaging.lambda) {
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				.withQueueUrl(queueName)
				.withWaitTimeSeconds(20)
			ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest)
			Collection<Message> messages = receiveMessageResult.getMessages()
			messages.each { Message message ->
				TrainingMessage trainingMessage = mapper.readValue(message.getBody(), TrainingMessage.class)
				try {
					alfredService.processResult(trainingMessage)
					sqsClient.deleteMessage(queueName, message.getReceiptHandle())
				} catch (Exception e) {
					log.error('Error occured during message processing', e)
				}
			}
		}
	}
}
