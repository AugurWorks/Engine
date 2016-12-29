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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SqsPollingJob {

	private static final Logger log = LoggerFactory.getLogger(SqsPollingJob)

	GrailsApplication grailsApplication
	AlfredService alfredService

	private final ObjectMapper mapper = new ObjectMapper()

	AmazonSQSClient sqsClient = new AmazonSQSClient()

	void execute() {
		String queueName = grailsApplication.config.messaging.sqsName
		log.info('Starting SQS polling for ' + queueName)
		while (true) {
			pollSqs(queueName)
		}
	}

	void pollSqs(String queueName) {
		Class<TrainingMessage> trainingMessageVersion = grailsApplication.config.messaging.version
		try {
			ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
				.withQueueUrl(queueName)
				.withWaitTimeSeconds(20)
				.withMaxNumberOfMessages(10)
			ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest)
			Collection<Message> messages = receiveMessageResult.getMessages()
			messages.each { Message message ->
				TrainingMessage trainingMessage = mapper.readValue(message.getBody(), trainingMessageVersion)
				try {
					alfredService.processResult(trainingMessage)
				} catch (AugurWorksException e) {
					log.warn(e.getMessage(), e)
				} catch (Exception e) {
					log.error(e.getMessage(), e)
				} finally {
					sqsClient.deleteMessage(queueName, message.getReceiptHandle())
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e)
			sleep 10000
		}
	}
}
