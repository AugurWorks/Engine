package com.augurworks.engine.jobs

import com.amazonaws.services.sqs.AmazonSQSClient
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.ReceiveMessageResult
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.messaging.TrainingMessage
import com.augurworks.engine.services.AlfredService
import com.fasterxml.jackson.databind.ObjectMapper
import grails.core.GrailsApplication

class SqsPollingJob {

	GrailsApplication grailsApplication
	AlfredService alfredService

	private final ObjectMapper mapper = new ObjectMapper()

	AmazonSQSClient sqsClient = new AmazonSQSClient()

	void execute() {
		String queueName = grailsApplication.config.messaging.sqsName
		log.info 'Starting SQS polling for ' + queueName
		while (true) {
			pollSqs(queueName)
		}
	}

	void pollSqs(String queueName) {
		try {
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				.withQueueUrl(queueName)
				.withWaitTimeSeconds(20)
				.withMaxNumberOfMessages(10)
			ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest)
			Collection<Message> messages = receiveMessageResult.getMessages()
			messages.each { Message message ->
				TrainingMessage trainingMessage = mapper.readValue(message.getBody(), TrainingMessage.class)
				try {
					alfredService.processResult(trainingMessage)
				} catch (AugurWorksException e) {
					log.warn e
				} catch (Exception e) {
					e.printStackTrace()
					log.error e
				} finally {
					sqsClient.deleteMessage(queueName, message.getReceiptHandle())
				}
			}
		} catch (Exception e) {
			log.error e
			sleep 10000
		}
	}
}
