package com.augurworks.engine.services

import com.amazonaws.services.sns.AmazonSNSClient
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.messaging.TrainingMessage
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.*
import grails.core.GrailsApplication
import grails.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.PostConstruct

@Transactional
class MessagingService {

	private static final Logger log = LoggerFactory.getLogger(MessagingService)

	private static final String SQS_NAME_KEY = 'sqsName'
	private static final String FLUENT_HOST_KEY = 'fluentHost'
	private static final String LOGGING_ENV_KEY = 'loggingEnv'

	public static final String ROOT_TRAINING_CHANNEL = "nets.training"
	public static final String ROOT_RESULTS_CHANNEL = "nets.results"

	public String trainingChannelName
	public String resultsChannelName

	private final ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)

	GrailsApplication grailsApplication
	AlfredService alfredService

	private Channel trainingChannel
	private Channel resultChannel
	
	@PostConstruct
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
		Class<TrainingMessage> trainingMessageVersion = grailsApplication.config.messaging.version
		log.debug("Starting results consumer")
		Consumer consumer = new DefaultConsumer(resultChannel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
					try {
						TrainingMessage message = mapper.readValue(body, trainingMessageVersion)
						alfredService.processResult(message)
					} catch (Exception e) {
						log.error(e.getMessage(), e)
					}
				}
			}
		try {
			resultChannel.basicConsume(resultsChannelName, true, consumer)
		} catch (IOException e) {
			log.error("Results consumer failed to initialize", e)
		}
	}

	void sendTrainingMessage(TrainingMessage message, boolean useLambda) {
		Map<String, String> metadata = [:]
		metadata.put(FLUENT_HOST_KEY, grailsApplication.config.logging.fluentHost)
		metadata.put(LOGGING_ENV_KEY, grailsApplication.config.logging.env)
		if (useLambda) {
			metadata.put(SQS_NAME_KEY, grailsApplication.config.messaging.sqsName)
			sendSNSTrainingMessage(message.withMetadata(metadata))
		} else {
			sendRabbitMQTrainingMessage(message.withMetadata(metadata))
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
