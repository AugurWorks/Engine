package com.augurworks.engine.services

import grails.converters.JSON
import grails.transaction.Transactional

import com.amazonaws.services.machinelearning.model.GetBatchPredictionResult
import com.amazonaws.services.machinelearning.model.GetMLModelResult
import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.AlgorithmResult
import com.augurworks.engine.domains.DataSet
import com.augurworks.engine.domains.MachineLearningModel
import com.augurworks.engine.domains.PredictedValue
import com.augurworks.engine.domains.RequestDataSet
import com.augurworks.engine.exceptions.AugurWorksException
import com.augurworks.engine.helper.AlgorithmType
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.helper.SingleDataRequest
import com.augurworks.engine.helper.SplineRequest

@Transactional
class MachineLearningService {

	DataRetrievalService dataRetrievalService
	AwsService awsService
	AutomatedService automatedService

	static final MACHINE_LEARNING_COMPLETE_STATUS = 'COMPLETED'

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String dataSourceId = createRequestDataSource(algorithmRequest, false)
		String modelId = awsService.createMLModel(dataSourceId)
		createAlgorithmResult(algorithmRequest, modelId, dataSourceId)
	}

	String createRequestDataSource(AlgorithmRequest algorithmRequest, boolean prediction) {
		return createRequestDataSource(algorithmRequest, prediction, new Date())
	}

	String createRequestDataSource(AlgorithmRequest algorithmRequest, boolean prediction, Date now) {
		File file = requestToCsv(algorithmRequest, prediction, now)
		String path = awsService.uploadToS3(file)
		file.delete()
		String dataSchema = createDataSchema(algorithmRequest, prediction)
		return awsService.createDataSource(path, dataSchema)
	}

	File requestToCsv(AlgorithmRequest algorithmRequest, boolean prediction, Date now) {
		File csv = File.createTempFile('AlgorithmRequest-' + algorithmRequest.id, '.csv')
		SplineRequest splineRequest = new SplineRequest(algorithmRequest: algorithmRequest, prediction: prediction, includeDependent: !prediction, now: now)
		Collection<RequestValueSet> dataSets = dataRetrievalService.smartSpline(splineRequest).sort { RequestValueSet requestValueSetA, RequestValueSet requestValueSetB ->
			return (requestValueSetB.name == algorithmRequest.dependantDataSet.ticker) <=> (requestValueSetA.name == algorithmRequest.dependantDataSet.ticker) ?: requestValueSetA.name <=> requestValueSetB.name
		}
		int rowNumber = dataSets*.values*.size().max()
		if (!automatedService.areDataSetsCorrectlySized(dataSets, rowNumber)) {
			throw new AugurWorksException('Request datasets aren\'t all the same length.')
		}
		csv << dataSets*.name.join(',') + '\n'
		(0..(rowNumber - 1)).each { int row ->
			csv << dataSets*.values.collect { it[row].value }.join(',') + '\n'
		}
		return csv
	}

	String createDataSchema(AlgorithmRequest algorithmRequest, boolean prediction) {
		Collection<RequestDataSet> requestDataSets = prediction ? algorithmRequest.independentRequestDataSets : algorithmRequest.requestDataSets
		Map schema = [
			version: '1.0',
			dataFormat: 'CSV',
			dataFileContainsHeader: true,
			attributes: requestDataSets*.dataSet.collect { DataSet dataSet ->
				return [
					attributeName: dataSet.ticker,
					attributeType: 'NUMERIC'
				]
			}
		]
		if (!prediction) {
			schema.targetAttributeName = algorithmRequest.dependantDataSet.ticker
		}
		return schema as JSON
	}

	void checkIncompleteAlgorithms() {
		Collection<AlgorithmResult> algorithmResults = AlgorithmResult.findAllByCompleteAndModelType(false, AlgorithmType.MACHINE_LEARNING)
		algorithmResults.each { AlgorithmResult algorithmResult ->
			try {
				checkAlgorithm(algorithmResult)
			} catch (AugurWorksException e) {
				log.warn 'Algorithm Result ' + algorithmResult.id + ' had an error: ' + e.getMessage()
				log.debug e.getStackTrace().join('\n      at ')
			} catch (e) {
				log.error e.getMessage()
				log.debug e.getStackTrace().join('\n      at ')
			}
		}
	}

	void createAlgorithmResult(AlgorithmRequest algorithmRequest, String modelId, String dataSourceId) {
		MachineLearningModel model = new MachineLearningModel(
			trainingDataSourceId: dataSourceId,
			modelId: modelId
		)
		model.save()
		AlgorithmResult algorithmResult = new AlgorithmResult([
			algorithmRequest: algorithmRequest,
			startDate: algorithmRequest.startDate,
			endDate: algorithmRequest.endDate,
			machineLearningModel: model,
			modelType: AlgorithmType.MACHINE_LEARNING
		])
		algorithmResult.save()
	}

	void checkAlgorithm(AlgorithmResult algorithmResult) {
		if (algorithmResult.machineLearningModel) {
			checkMachineLearningAlgorithm(algorithmResult)
		}
	}

	void checkMachineLearningAlgorithm(AlgorithmResult algorithmResult) {
		if (algorithmResult.machineLearningModel.batchPredictionId) {
			checkMachineLearningPrediction(algorithmResult)
		} else {
			checkMachineLearningModel(algorithmResult)
		}
	}

	void checkMachineLearningModel(AlgorithmResult algorithmResult) {
		GetMLModelResult mlModel = awsService.getMLModel(algorithmResult.machineLearningModel.modelId)
		if (mlModel.getStatus() == MACHINE_LEARNING_COMPLETE_STATUS) {
			log.info 'Machine learning model complete, generating batch prediction'
			generateMachineLearningResult(algorithmResult)
		}
	}

	void generateMachineLearningResult(AlgorithmResult algorithmResult) {
		String dataSourceId = createRequestDataSource(algorithmResult.algorithmRequest, true, algorithmResult.dateCreated)
		String batchPredictionId = awsService.createBatchPrediction(dataSourceId, algorithmResult.machineLearningModel.modelId)
		MachineLearningModel model = algorithmResult.machineLearningModel
		model.predictionDataSourceId = dataSourceId
		model.batchPredictionId = batchPredictionId
		model.save()
		algorithmResult.save()
	}

	void checkMachineLearningPrediction(AlgorithmResult algorithmResult) {
		GetBatchPredictionResult batchPrediction = awsService.getBatchPrediction(algorithmResult.machineLearningModel.batchPredictionId)
		if (batchPrediction.getStatus() == MACHINE_LEARNING_COMPLETE_STATUS) {
			log.info 'Machine learning batch prediction complete'
			File resultsFile = awsService.getBatchPredictionResults(algorithmResult.machineLearningModel.batchPredictionId)
			Collection<Double> predictions = parsePredictionOutputFile(resultsFile)
			addPredictionsToAlgorithmResult(algorithmResult, predictions)
			cleanupMachineLearning(algorithmResult)
			algorithmResult.complete = true
			algorithmResult.save(flush: true)
			automatedService.postProcessing(algorithmResult)
		}
	}

	Collection<Double> parsePredictionOutputFile(File predictionFile) {
		Collection<Double> predictions = []
		predictionFile.eachLine { String line, int num ->
			if (num != 1) {
				predictions << line.toDouble()
			}
		}
		return predictions
	}

	void addPredictionsToAlgorithmResult(AlgorithmResult algorithmResult, Collection<Double> predictions) {
		RequestDataSet predictionSet = algorithmResult.algorithmRequest.dependentRequestDataSet
		SingleDataRequest singleDataRequest = new SingleDataRequest(
			symbolResult: predictionSet.toSymbolResult(),
			offset: predictionSet.offset,
			startDate: algorithmResult.algorithmRequest.getStartDate(algorithmResult.dateCreated),
			endDate: algorithmResult.algorithmRequest.getEndDate(algorithmResult.dateCreated),
			unit: algorithmResult.algorithmRequest.unit,
			minOffset: predictionSet.offset,
			maxOffset: predictionSet.offset,
			aggregation: predictionSet.aggregation
		)
		RequestValueSet requestValueSet = dataRetrievalService.getSingleRequestValues(singleDataRequest)
		Collection<Date> predictionDates = requestValueSet.dates
		createPredictedValues(algorithmResult, predictionDates, predictions)
	}

	void createPredictedValues(AlgorithmResult algorithmResult, Collection<Date> predictionDates, Collection<Double> predictions) {
		predictions.eachWithIndex { Double prediction, int index ->
			Date date = index < predictionDates.size() ? predictionDates[index] : Common.calculatePredictionDate(algorithmResult.algorithmRequest.unit, predictionDates.last(), index - predictionDates.size() + 1)
			new PredictedValue(date: date, value: prediction, algorithmResult: algorithmResult).save()
		}
	}

	void cleanupMachineLearning(AlgorithmResult algorithmResult) {
		MachineLearningModel model = algorithmResult.machineLearningModel
		awsService.deleteDatasource(model.trainingDataSourceId)
		awsService.deleteModel(model.modelId)
		awsService.deleteDatasource(model.predictionDataSourceId)
		awsService.deleteBatchPrediction(model.batchPredictionId)
		algorithmResult.machineLearningModel = null
		algorithmResult.save()
		model.delete()
	}
}
