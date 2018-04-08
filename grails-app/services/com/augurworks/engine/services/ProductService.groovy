package com.augurworks.engine.services

import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.Product
import com.augurworks.engine.domains.ProductResult
import com.augurworks.engine.instrumentation.Instrumentation
import com.augurworks.engine.model.prediction.RuleEvaluationAction
import com.augurworks.engine.slack.SlackMessage
import com.timgroup.statsd.StatsDClient
import grails.core.GrailsApplication
import grails.transaction.Transactional
import grails.util.Holders
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

@Transactional
class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService)

	private final StatsDClient statsdClient = Instrumentation.statsdClient

	GrailsApplication grailsApplication

	void createProductResult(AlgorithmResult algorithmResult) {
		MDC.put('product', algorithmResult.algorithmRequest.product.id.toString())
		log.debug('Running product post processing for algorithm request ' + algorithmResult.id + ' and product ' + algorithmResult.algorithmRequest.product.name)
		Product product = algorithmResult.algorithmRequest.product
		ProductResult productResult = ProductResult.findByProductAndAdjustedDateCreated(product, algorithmResult.adjustedDateCreated)
		if (productResult) {
			MDC.put('productResult', productResult.id.toString())

			productResult[algorithmResult.algorithmRequest.name.toLowerCase().contains('close') ? 'closeResult' : 'realTimeResult'] = algorithmResult
			productResult.save()
			log.info('Processing complete product result')
			processProductResult(productResult)
		} else {
			log.debug('Creating new product result')
			List<ProductResult> previousProductResults = ProductResult.findAllByProduct(product, [sort: 'adjustedDateCreated', order: 'desc', max: 1])
			productResult = new ProductResult(
					product: product,
					adjustedDateCreated: algorithmResult.adjustedDateCreated,
					previousProductResult: previousProductResults.size() == 1 ? previousProductResults.get(0) : null
			)
			productResult[algorithmResult.algorithmRequest.name.toLowerCase().contains('close') ? 'closeResult' : 'realTimeResult'] = algorithmResult
			productResult.save()
		}
	}

	private void processProductResult(ProductResult productResult) {
		if (grailsApplication.config.slack.webhook) {
			sendToSlack(productResult.product, productResult.action, productResult.slackChannel)
		}
	}

	private void sendToSlack(Product product, RuleEvaluationAction action, String slackChannel) {
		statsdClient.increment('count.slack.messages.sent')
		Map slackMap = this.getSlackMap(product, action, slackChannel)
		new SlackMessage(slackMap.message, slackMap.channel).withBotName('Engine Predictions').withColor(slackMap.color).withTitle(slackMap.title).withLink(slackMap.link).send()
	}

	private Map getSlackMap(Product product, RuleEvaluationAction action, String slackChannel) {
		return [
				channel: slackChannel ?: Holders.config.augurworks.predictions.channel,
				color: action.color,
				title: action.name() + ': ' + product.name
		]
	}
}
