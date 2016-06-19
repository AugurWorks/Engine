package com.augurworks.engine.services

import grails.transaction.Transactional

import javax.annotation.PostConstruct

import org.codehaus.groovy.grails.commons.GrailsApplication

import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.messaging.TrainingMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.AlreadyClosedException
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope

@Transactional
class MessagingService {

	public static final String TRAINING_CHANNEL = "nets.training"
	public static final String RESULTS_CHANNEL = "nets.results"

	private final ObjectMapper mapper = new ObjectMapper()

	GrailsApplication grailsApplication
	AlfredService alfredService

	private Channel trainingChannel
	private Channel resultChannel

	@PostConstruct
	private void init() {
		log.info('Initializing RabbitMQ connections')
		try {
			ConnectionFactory factory = new ConnectionFactory()
			factory.setUsername(grailsApplication.config.rabbitmq.username)
			factory.setPassword(grailsApplication.config.rabbitmq.password)
			factory.setHost(grailsApplication.config.rabbitmq.hostname)
			factory.setPort(grailsApplication.config.rabbitmq.port)
			Connection connection = factory.newConnection()

			trainingChannel = connection.createChannel()
			trainingChannel.queueDeclare(TRAINING_CHANNEL, false, false, false, null)

			resultChannel = connection.createChannel()
			resultChannel.queueDeclare(RESULTS_CHANNEL, false, false, false, null)

			initResultConsumer(resultChannel)
		} catch (Exception e) {
			trainingChannel = null
			resultChannel = null
			log.error("Could not connect to RabbitMQ", e)
		}
	}

	private void initResultConsumer(Channel resultChannel) {
		log.info("Starting results consumer")
		Consumer consumer = new DefaultConsumer(trainingChannel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
					TrainingMessage message = mapper.readValue(body, TrainingMessage.class)
					alfredService.processResult(message)
				}
			}
		try {
			resultChannel.basicConsume(RESULTS_CHANNEL, true, consumer)
		} catch (IOException e) {
			log.error("Results consumer failed to initialize", e)
		}
	}


	void sendTrainingMessage(TrainingMessage message) {
		String errorMessage = 'Unable to submit training message, the messaging bus is currently unavailable'
		if (trainingChannel == null) {
			throw new AugurWorksException(errorMessage)
		}
		try {
			trainingChannel.basicPublish("", TRAINING_CHANNEL, null, mapper.writeValueAsBytes(message))
		} catch (AlreadyClosedException e) {
			init()
			sendTrainingMessage(message)
		} catch (IOException e) {
			log.error("An error occurred when publishing a message for net {}", message.getNetId(), e)
			throw new AugurWorksException(errorMessage)
		}
	}
}
