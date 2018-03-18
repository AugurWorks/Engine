package com.augurworks.engine.services

import com.augurworks.engine.data.ActualValue
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.Product
import com.augurworks.engine.model.prediction.PredictionRuleResult
import grails.core.GrailsApplication
import grails.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

@Transactional
class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService)

	GrailsApplication grailsApplication
	ActualValueService actualValueService

	void runProductRules(AlgorithmResult algorithmResult) {
		MDC.put('product', algorithmResult.algorithmRequest.product.id.toString())
		log.debug('Running product post processing for algorithm request ' + algorithmResult.id + ' and product ' + algorithmResult.algorithmRequest.product.name)
		Product product = algorithmResult.algorithmRequest.product
		List<AlgorithmResult> algorithmResults = AlgorithmResult.executeQuery('SELECT ar FROM AlgorithmRequest as areq INNER JOIN areq.algorithmResults as ar where areq.product = :product and ar.adjustedDateCreated = :date', [
				product: product,
				date: algorithmResult.adjustedDateCreated
		])
		if (algorithmResults*.algorithmRequest.unique().size() == product.algorithmRequests.size()) {
			List<PredictionRuleResult> results = algorithmResults.collect { result(it) }
			if (results*.action.unique().size() == 1) {
				if (results*.action.unique().get(0) == null) {
					log.info('All algorithm results for product ' + product.name + ' have completed, but there are no actions')
				} else {
					log.info('All algorithm results for product ' + product.name + ' have completed with result ' + results*.action.unique().get(0))
				}
			} else {
				log.debug('Not all algorithm results have completed')
			}
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
}
