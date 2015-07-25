package com.augurworks.engine

import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.services.AwsService
import com.amazonaws.services.machinelearning.model.GetMLModelResult

import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class MachineLearningService {

	DataRetrievalService dataRetrievalService
	AwsService awsService

	void createAlgorithm(AlgorithmRequest algorithmRequest) {
		File file = requestToCsv(algorithmRequest)
		String path = awsService.uploadToS3(file)
		file.delete()
		String dataSchema = createDataSchema(algorithmRequest)
		String dataSourceId = awsService.createDataSource(path, dataSchema)
		String modelId = awsService.createMLModel(dataSourceId)
		createAlgorithmResult(algorithmRequest, modelId)
	}

	File requestToCsv(AlgorithmRequest algorithmRequest) {
		File csv = File.createTempFile('AlgorithmRequest-' + algorithmRequest.id, '.csv')
		Collection<RequestValueSet> dataSets = dataRetrievalService.smartSpline(algorithmRequest).sort { RequestValueSet requestValueSetA, RequestValueSet requestValueSetB ->
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

	String createDataSchema(AlgorithmRequest algorithmRequest) {
		Map schema = [
			version: '1.0',
			targetAttributeName: algorithmRequest.dependantDataSet.ticker,
			dataFormat: 'CSV',
			dataFileContainsHeader: true,
			attributes: algorithmRequest.requestDataSets*.dataSet.collect { DataSet dataSet ->
				return [
					attributeName: dataSet.ticker,
					attributeType: 'NUMERIC'
				]
			}
		]
		return schema as JSON
	}

	boolean areDataSetsCorrectlySized(Collection<Map> dataSets, int rowNumber) {
		return dataSets*.values*.size().every { it == rowNumber }
	}

	void checkIncompleteAlgorithms() {
		Collection<AlgorithmResult> algorithmResults = AlgorithmResult.list().grep { !it.complete }
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
		GetMLModelResult mlModel = awsService.getMLModel(algorithmResult.modelId)
		algorithmResult.modelStatus = mlModel.getStatus()
		algorithmResult.save()
	}
}
