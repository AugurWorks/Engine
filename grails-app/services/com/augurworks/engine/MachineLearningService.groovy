package com.augurworks.engine

import grails.converters.JSON
import grails.transaction.Transactional

import com.amazonaws.services.machinelearning.model.GetBatchPredictionResult
import com.amazonaws.services.machinelearning.model.GetMLModelResult
import com.augurworks.engine.helper.Common
import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.services.AwsService

@Transactional
class MachineLearningService {

	DataRetrievalService dataRetrievalService
	AwsService awsService

	static final MACHINE_LEARNING_COMPLETE_STATUS = 'COMPLETED'

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		String dataSourceId = createRequestDataSource(algorithmRequest, false)
		String modelId = awsService.createMLModel(dataSourceId)
		createAlgorithmResult(algorithmRequest, modelId)
	}

	String createRequestDataSource(AlgorithmRequest algorithmRequest, boolean prediction) {
		File file = requestToCsv(algorithmRequest, prediction)
		String path = awsService.uploadToS3(file)
		file.delete()
		String dataSchema = createDataSchema(algorithmRequest, prediction)
		return awsService.createDataSource(path, dataSchema)
	}

	File requestToCsv(AlgorithmRequest algorithmRequest, boolean prediction) {
		File csv = File.createTempFile('AlgorithmRequest-' + algorithmRequest.id, '.csv')
		Collection<RequestValueSet> dataSets = dataRetrievalService.smartSpline(algorithmRequest, prediction).sort { RequestValueSet requestValueSetA, RequestValueSet requestValueSetB ->
			return (requestValueSetB.name == algorithmRequest.dependantDataSet.ticker) <=> (requestValueSetA.name == algorithmRequest.dependantDataSet.ticker) ?: requestValueSetA.name <=> requestValueSetB.name
		}
		int rowNumber = dataSets*.values*.size().max()
		if (!areDataSetsCorrectlySized(dataSets, rowNumber)) {
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

	boolean areDataSetsCorrectlySized(Collection<Map> dataSets, int rowNumber) {
		return dataSets*.values*.size().every { it == rowNumber }
	}

	void checkIncompleteAlgorithms() {
		Collection<AlgorithmResult> algorithmResults = AlgorithmResult.findAllByComplete(false)
		algorithmResults.each { AlgorithmResult algorithmResult ->
			checkAlgorithm(algorithmResult)
		}
	}

	void createAlgorithmResult(AlgorithmRequest algorithmRequest, String modelId) {
		AlgorithmResult algorithmResult = new AlgorithmResult([
			algorithmRequest: algorithmRequest,
			modelId: modelId
		])
		algorithmResult.save();
	}

	void checkAlgorithm(AlgorithmResult algorithmResult) {
		if (algorithmResult.machineLearning) {
			checkMachineLearningAlgorithm(algorithmResult)
		}
	}

	void checkMachineLearningAlgorithm(AlgorithmResult algorithmResult) {
		if (algorithmResult.batchPredictionId) {
			checkMachineLearningPrediction(algorithmResult)
		} else {
			checkMachineLearningModel(algorithmResult)
		}
	}

	void checkMachineLearningModel(AlgorithmResult algorithmResult) {
		GetMLModelResult mlModel = awsService.getMLModel(algorithmResult.modelId)
		if (mlModel.getStatus() == MACHINE_LEARNING_COMPLETE_STATUS) {
			String batchPredictionId = generateMachineLearningResult(algorithmResult)
			algorithmResult.batchPredictionId = batchPredictionId
			algorithmResult.save()
		}
	}

	String generateMachineLearningResult(AlgorithmResult algorithmResult) {
		String dataSourceId = createRequestDataSource(algorithmResult.algorithmRequest, true)
		return awsService.createBatchPrediction(dataSourceId, algorithmResult.modelId)
	}

	void checkMachineLearningPrediction(AlgorithmResult algorithmResult) {
		GetBatchPredictionResult batchPrediction = awsService.getBatchPrediction(algorithmResult.batchPredictionId)
		if (batchPrediction.getStatus() == MACHINE_LEARNING_COMPLETE_STATUS) {
			File resultsFile = awsService.getBatchPredictionResults(algorithmResult.batchPredictionId)
			algorithmResult.complete = true
			algorithmResult.save()
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

	void createPredictedValues(AlgorithmResult algorithmResult, Collection<Date> predictionDates, Collection<Double> predictions) {
		predictions.eachWithIndex { Double prediction, int index ->
			Date date = index < predictionDates.size() ? predictionDates[index] : Common.addDaysToDate(predictionDates.last(), index - predictionDates.size() + 1)
			new PredictedValue(date: date, value: prediction, algorithmResult: algorithmResult).save()
		}
	}
}
