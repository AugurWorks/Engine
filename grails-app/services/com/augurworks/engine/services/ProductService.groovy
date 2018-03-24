package com.augurworks.engine.services

import com.augurworks.engine.data.ActualValue
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.Product
import com.augurworks.engine.domains.ProductResult
import com.augurworks.engine.instrumentation.Instrumentation
import com.augurworks.engine.model.prediction.PredictionRuleResult
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
	ActualValueService actualValueService

	void createProductResult(AlgorithmResult algorithmResult) {
		MDC.put('product', algorithmResult.algorithmRequest.product.id.toString())
		log.debug('Running product post processing for algorithm request ' + algorithmResult.id + ' and product ' + algorithmResult.algorithmRequest.product.name)
		Product product = algorithmResult.algorithmRequest.product
		ProductResult productResult = ProductResult.findByProductAndAdjustedDateCreated(product, algorithmResult.adjustedDateCreated)
		if (productResult) {
			MDC.put('productResult', productResult.id.toString())
			productResult.algorithmResults.push(algorithmResult)
			productResult.save()
			log.info('Processing complete product result')
			processProductResult(productResult)
		} else {
			log.debug('Creating new product result')
			List<ProductResult> previousProductResults = ProductResult.findAllByProduct(product, [sort: 'adjustedDateCreated', order: 'desc', max: 1])
			new ProductResult(
					product: product,
					adjustedDateCreated: algorithmResult.adjustedDateCreated,
					previousProductResult: previousProductResults.size() == 1 ? previousProductResults.get(0) : null,
					algorithmResults: [algorithmResult]
			).save()
		}
	}

	private void processProductResult(ProductResult productResult) {
		List<PredictionRuleResult> results = productResult.algorithmResults.collect { result(it) }
		if (grailsApplication.config.slack.webhook) {
			sendToSlack(productResult.product, productResult.algorithmResults, results*.action.unique().get(0))
		}
	}

	private PredictionRuleResult processPredictionRuleResult(ProductResult productResult) {
		if (productResult.isTooVolatile()) {
			return new PredictionRuleResult('Current run is too volatile', RuleEvaluationAction.HOLD)
		}
		if (productResult.previousRun && productResult.previousRun.isTooVolatile()) {
			return new PredictionRuleResult('Previous run is too volatile', RuleEvaluationAction.HOLD)
		}

	}

	private PredictionRuleResult result(AlgorithmResult algorithmResult) {
		List<AlgorithmResult> previousAlgorithmResult = AlgorithmResult.findAllByAlgorithmRequest(algorithmResult.algorithmRequest, [
				max: 2, sort: 'dateCreated', order: 'desc'
		])
		Optional<ActualValue> actualValue = actualValueService.getActual(algorithmResult)
		if (!actualValue.isPresent()) {
			return new PredictionRuleResult('Current or previous run data is missing')
		}
		Optional<ActualValue> previousActualValue = previousAlgorithmResult.size() != 2 ? Optional.empty() : actualValueService.getActual(previousAlgorithmResult.get(1))
		return PredictionRuleResult.create(algorithmResult.algorithmRequest, actualValue.get(), previousActualValue)
	}

	private void sendToSlack(ActualValue actualValue = null, Optional<ActualValue> previousActualValue = Optional.empty()) {
		statsdClient.increment('count.slack.messages.sent')
		Map slackMap = this.getSlackMap(actualValue, previousActualValue)
		new SlackMessage(slackMap.message, slackMap.channel).withBotName('Engine Predictions').withColor(slackMap.color).withTitle(slackMap.title).withLink(slackMap.link).send()
	}

	private Map getSlackMap(Product product, List<AlgorithmResult> algorithmResults, RuleEvaluationAction action) {
		return [
				channel: algorithmResults*.algorithmRequest*.slackChannel.unique().get(0) ?: Holders.config.augurworks.predictions.channel,
				color: action.color,
				title: action.name() + ': ' + product.name
		]
	}
}
