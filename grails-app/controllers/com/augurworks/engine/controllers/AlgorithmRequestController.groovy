package com.augurworks.engine.controllers

import com.augurworks.engine.data.SplineRequest
import com.augurworks.engine.data.SplineType
import com.augurworks.engine.domains.*
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.*
import com.augurworks.engine.services.*
import grails.converters.JSON
import grails.core.GrailsApplication
import org.apache.commons.lang.StringUtils
import org.quartz.CronExpression
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AlgorithmRequestController {

	private static final Logger log = LoggerFactory.getLogger(AlgorithmRequestController)

	private static final Integer PAGE_SIZE = 5

	GrailsApplication grailsApplication
	DataRetrievalService dataRetrievalService
	AutomatedService automatedService
	AutoKickoffService autoKickoffService

	def index() {
		[requests: AlgorithmRequest.list()]
	}

	def show(AlgorithmRequest algorithmRequest) {
		if (!algorithmRequest) {
			render(view: '/404')
		}
		[
            algorithm: algorithmRequest,
            algorithmResults: grailsApplication.config.graphs.on.toBoolean() ? getResults(algorithmRequest, 0) : [],
            total: grailsApplication.config.graphs.on.toBoolean() ? AlgorithmResult.countByAlgorithmRequest(algorithmRequest) : 0,
            graphsOn: grailsApplication.config.graphs.on.toBoolean()
        ]
	}

	def showMore(AlgorithmRequest algorithmRequest, Integer page) {
		render(template: '/layouts/resultCards', model: [results: getResults(algorithmRequest, page)])
	}

	private Collection<AlgorithmResult> getResults(AlgorithmRequest algorithmRequest, Integer page) {
		return AlgorithmResult.findAllByAlgorithmRequest(algorithmRequest, [offset: page * PAGE_SIZE, sort: 'dateCreated', order: 'desc', max: PAGE_SIZE]);
	}

	def run(AlgorithmRequest algorithmRequest, String type) {
		try {
			AlgorithmType algorithmType = AlgorithmType.findByName(type)
			automatedService.runAlgorithm(algorithmRequest, algorithmType)
			render([ok: true] as JSON)
		} catch (AugurWorksException e) {
			log.error(e.getMessage(), e)
			render([ok: false, error: e.getMessage()] as JSON)
		} catch (e) {
			log.error(e.getMessage(), e)
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	def resultCard(AlgorithmResult algorithmResult) {
		render(template: '/layouts/resultCard', model: [result: algorithmResult, title: true])
	}

	def create(AlgorithmRequest algorithmRequest) {
		[algorithmRequest: algorithmRequest]
	}

	def saveRequest(
			String alfredEnvironment,
			String cronExpression,
			String slackChannel,
			Integer trainingRounds,
			Double learningConstant,
			Integer depth,
			Long product,
			Long id) {
		try {
			AlgorithmRequest algorithmRequest = AlgorithmRequest.get(id)
			if (alfredEnvironment) {
				algorithmRequest.alfredEnvironment = AlfredEnvironment.findByName(alfredEnvironment)
			}
			if (params.cronAlgorithms) {
				algorithmRequest.cronAlgorithms = JSON.parse(params.cronAlgorithms).collect {
					AlgorithmType.findByName(it)
				}
			}
			algorithmRequest.cronExpression = cronExpression
			if (slackChannel != null) {
				algorithmRequest.slackChannel = slackChannel ? '#' + slackChannel.replaceAll('#', '') : null
			}
			algorithmRequest.trainingRounds = trainingRounds
			algorithmRequest.learningConstant = learningConstant
			algorithmRequest.depth = depth
			algorithmRequest.product = Product.findById(product)
			algorithmRequest.tags.clear()
			List<RequestTag> requestTags = JSON.parse(params.tags).grep { StringUtils.isNotBlank(it) }.collect { it.trim() }.unique().collect { new RequestTag(name: it, algorithmRequest: algorithmRequest).save() }
			algorithmRequest.save()
			autoKickoffService.clearJob(algorithmRequest)
			if (algorithmRequest.cronExpression) {
				autoKickoffService.scheduleKickoffJob(algorithmRequest)
			}
			render([ok: true, id: algorithmRequest.id] as JSON)
		} catch (e) {
			log.error(e.getMessage(), e)
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	def submitRequest(String name, int startOffset, int endOffset, String unit, String splineType, String alfredEnvironment, String cronExpression, Long product, String slackChannel, Integer trainingRounds, Double learningConstant, Integer depth, Long id, boolean overwrite) {
		try {
			Collection<String> cronAlgorithms = JSON.parse(params.cronAlgorithms)
			Collection<Map> rawDataSets = JSON.parse(params.dataSets)
			Collection<RequestDataSet> dataSets = rawDataSets.collect { Map dataSet ->
				return parseDataSet(dataSet)
			}
			Map dependant = rawDataSets.grep { it.dependant }.first()
			String dependantSymbol = dependant.symbol + ' - ' + DataType.findByName(dependant.dataType).name()
			if (overwrite && id) {
				AlgorithmRequest deleteRequest = AlgorithmRequest.get(id)
				autoKickoffService.clearJob(deleteRequest)
				deleteRequest?.delete(flush: true)
			}
			AlgorithmRequest algorithmRequest = constructAlgorithmRequest(name, startOffset, endOffset, Unit[unit], SplineType[splineType], AlfredEnvironment.findByName(alfredEnvironment), cronExpression, product, slackChannel, cronAlgorithms, dependantSymbol, trainingRounds, learningConstant, depth)
			algorithmRequest.save()
			JSON.parse(params.tags).grep { StringUtils.isNotBlank(it) }.collect { it.trim() }.unique().collect { new RequestTag(name: it, algorithmRequest: algorithmRequest).save() }
			if (algorithmRequest.hasErrors()) {
				throw new AugurWorksException('The request could not be created, please check that the name is unique.')
			}
			algorithmRequest.updateDataSets(dataSets)
			if (algorithmRequest.cronExpression) {
				autoKickoffService.scheduleKickoffJob(algorithmRequest)
			}
			render([ok: true, id: algorithmRequest.id] as JSON)
		} catch (e) {
			log.error(e.getMessage(), e)
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	def checkRequest(int startOffset, int endOffset, String unit, String splineType, String cronExpression) {
		try {
			if (cronExpression && !CronExpression.isValidExpression(cronExpression)) {
				throw new AugurWorksException('Invalid Cron Expression')
			}
			Collection<Map> rawDataSets = JSON.parse(params.dataSets)
			Collection<RequestDataSet> dataSets = rawDataSets.collect { Map dataSet ->
				return parseDataSet(dataSet)
			}
			Map dependant = rawDataSets.grep { it.dependant }.first()
			String dependantSymbol = dependant.symbol + ' - ' + DataType.findByName(dependant.dataType).name()
			AlgorithmRequest algorithmRequest = constructAlgorithmRequest(null, startOffset, endOffset, Unit[unit], SplineType[splineType], null, cronExpression, null, null, [], dependantSymbol, null, null, null)
			algorithmRequest.updateDataSets(dataSets, false)
			SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest)
			dataRetrievalService.smartSpline(splineRequest)
			render([ok: true] as JSON)
		} catch (e) {
			log.error(e.getMessage(), e)
			render([ok: false, error: e.getMessage()] as JSON)
		}
	}

	private RequestDataSet parseDataSet(Map dataSet) {
		return new RequestDataSet(
			symbol: dataSet.symbol,
			name: dataSet.name,
			datasource: Datasource[dataSet.datasource.toUpperCase()],
			offset: dataSet.offset,
			aggregation: Aggregation.findByName(dataSet.aggregation),
			dataType: DataType.findByName(dataSet.dataType)
		)
	}

	private AlgorithmRequest constructAlgorithmRequest(
			String name,
			int startOffset,
			int endOffset,
			Unit unit,
			SplineType splineType,
			AlfredEnvironment alfredEnvironment,
			String cronExpression,
			Long product,
			String slackChannel,
			Collection<String> cronAlgorithms,
			String dependantSymbol,
			Integer trainingRounds,
			Double learningConstant,
			Integer depth) {
		Map parameters = [
			name: name,
			startOffset: startOffset,
			endOffset: endOffset,
			unit: unit,
			splineType: splineType,
			alfredEnvironment: alfredEnvironment,
			cronExpression: cronExpression,
			product: Product.findById(product),
			slackChannel: slackChannel,
			cronAlgorithms: cronAlgorithms.collect { AlgorithmType.findByName(it) },
			dependantSymbol: dependantSymbol,
			trainingRounds: trainingRounds,
			learningConstant: learningConstant,
			depth: depth
		]
		return new AlgorithmRequest(parameters)
	}

	def deleteRequest(AlgorithmRequest algorithmRequest) {
		try {
			autoKickoffService.clearJob(algorithmRequest)
			algorithmRequest.delete(flush: true)
			render([ok: true] as JSON)
		} catch(e) {
			log.error(e.getMessage(), e)
			render([ok: false] as JSON)
		}
	}

	def deleteResult(AlgorithmResult algorithmResult) {
		try {
			algorithmResult.delete(flush: true)
			render([ok: true] as JSON)
		} catch(e) {
			log.error(e.getMessage(), e)
			render([ok: false] as JSON)
		}
	}

	def searchSymbol(String keyword) {
		render([
			success: true,
			results: keyword.size() > 1 ? dataRetrievalService.searchSymbol(keyword)*.toResultMap() : []
		] as JSON)
	}
}
