package com.augurworks.engine

import com.augurworks.engine.helper.RequestValueSet
import com.augurworks.engine.services.AwsService
import grails.transaction.Transactional

@Transactional
class MachineLearningService {

	DataRetrievalService dataRetrievalService
	AwsService awsService

	void createAlgorithm(algorithmRequest) {
		File file = requestToCsv(algorithmRequest)
		awsService.uploadToS3(file)
		file.delete()
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

	boolean areDataSetsCorrectlySized(Collection<Map> dataSets, int rowNumber) {
		return dataSets*.values*.size().every { it == rowNumber }
	}
}
