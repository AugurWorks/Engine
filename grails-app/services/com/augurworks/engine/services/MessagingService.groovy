package com.augurworks.engine.services

import grails.transaction.Transactional

import javax.annotation.PostConstruct

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.MDC

import com.amazonaws.services.sns.AmazonSNSClient
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.messaging.TrainingMessage
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.ShutdownListener
import com.rabbitmq.client.ShutdownSignalException

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
		if (!grailsApplication.config.messaging.lambda) {
			rabbitMQInit()
		}
	}
	
	private void rabbitMQInit() {
		log.info('Initializing RabbitMQ connections')
		try {
			ConnectionFactory factory = new ConnectionFactory()
			factory.setUsername(grailsApplication.config.rabbitmq.username)
			factory.setPassword(grailsApplication.config.rabbitmq.password)
			factory.setHost(grailsApplication.config.rabbitmq.hostname)
			factory.setPort(grailsApplication.config.rabbitmq.port)
			factory.setRequestedHeartbeat(1)
			factory.setConnectionTimeout(5000)
			factory.setAutomaticRecoveryEnabled(true)
			factory.setTopologyRecoveryEnabled(true)
			Connection connection = factory.newConnection()

			connection.addShutdownListener(new ShutdownListener() {
				public void shutdownCompleted(ShutdownSignalException e) {
					log.error('RabbitMQ connection lost', e)
				}
			});

			String channelPostfix = grailsApplication.config.rabbitmq.env ? '.' + grailsApplication.config.rabbitmq.env.toLowerCase() : ''

			trainingChannelName = ROOT_TRAINING_CHANNEL + channelPostfix
			resultsChannelName = ROOT_RESULTS_CHANNEL + channelPostfix


			log.info('Connecting to RabbitMQ channel ' + trainingChannelName)
			trainingChannel = connection.createChannel()
			trainingChannel.queueDeclare(trainingChannelName, true, false, false, null)

			log.info('Connecting to RabbitMQ channel ' + resultsChannelName)
			resultChannel = connection.createChannel()
			resultChannel.queueDeclare(resultsChannelName, true, false, false, null)

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
		if (grailsApplication.config.messaging.lambda) {
			sendSNSTrainingMessage(message)
		} else {
			sendRabbitMQTrainingMessage(message)
		}
	}

	void sendRabbitMQTrainingMessage(TrainingMessage message) {
		String errorMessage = 'Unable to submit training message, the messaging bus is currently unavailable'
		if (trainingChannel == null) {
			throw new AugurWorksException(errorMessage)
		}
		try {
			trainingChannel.basicPublish("", trainingChannelName, null, mapper.writeValueAsBytes(message))
		} catch (IOException e) {
			log.error("An error occurred when publishing a message for net {}", message.getNetId(), e)
			throw new AugurWorksException(errorMessage)
		}
	}
	
	void sendSNSTrainingMessage(TrainingMessage message) {
		AmazonSNSClient snsClient = new AmazonSNSClient()
		String snsTopicArn = grailsApplication.config.messaging.snsTopicArn
		snsClient.publish(snsTopicArn, mapper.writeValueAsString(message))
	}
}
