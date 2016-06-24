package com.augurworks.engine.services

import grails.transaction.Transactional

import javax.annotation.PostConstruct

import org.codehaus.groovy.grails.commons.GrailsApplication

import org.slf4j.MDC

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

	public static final String ROOT_TRAINING_CHANNEL = "nets.training"
	public static final String ROOT_RESULTS_CHANNEL = "nets.results"

	public String trainingChannelName
	public String resultsChannelName

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

			String channelPostfix = grailsApplication.config.rabbitmq.env ? '.' + grailsApplication.config.rabbitmq.env : ''

			trainingChannelName = ROOT_TRAINING_CHANNEL + channelPostfix
			resultsChannelName = ROOT_RESULTS_CHANNEL + channelPostfix


			log.info('Connecting to RabbitMQ channel ' + trainingChannelName)
			trainingChannel = connection.createChannel()
			trainingChannel.queueDeclare(trainingChannelName, false, false, false, null)

			log.info('Connecting to RabbitMQ channel ' + resultsChannelName)
			resultChannel = connection.createChannel()
			resultChannel.queueDeclare(resultsChannelName, false, false, false, null)

			initResultConsumer(resultChannel)
		} catch (Exception e) {
			trainingChannel = null
			resultChannel = null
			log.error("Could not connect to RabbitMQ", e)
		}
	}

	private void initResultConsumer(Channel resultChannel) {
		log.debug("Starting results consumer")
		Consumer consumer = new DefaultConsumer(resultChannel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
					try {
						TrainingMessage message = mapper.readValue(body, TrainingMessage.class)
						MDC.put('netId', message.getNetId())
						log.debug 'Consuming message for net ' + message.getNetId()
						alfredService.processResult(message)
						MDC.remove('netId')
					} catch (Exception e) {
						log.error e
					}
				}
			}
		try {
			resultChannel.basicConsume(resultsChannelName, true, consumer)
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
			trainingChannel.basicPublish("", trainingChannelName, null, mapper.writeValueAsBytes(message))
		} catch (AlreadyClosedException e) {
			init()
			sendTrainingMessage(message)
		} catch (IOException e) {
			log.error("An error occurred when publishing a message for net {}", message.getNetId(), e)
			throw new AugurWorksException(errorMessage)
		}
	}
}
