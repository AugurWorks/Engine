package com.augurworks.engine.services

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.machinelearning.AmazonMachineLearningClient
import com.amazonaws.services.machinelearning.model.CreateDataSourceFromS3Request
import com.amazonaws.services.machinelearning.model.CreateDataSourceFromS3Result
import com.amazonaws.services.machinelearning.model.CreateMLModelRequest
import com.amazonaws.services.machinelearning.model.S3DataSpec
import com.amazonaws.services.machinelearning.model.MLModelType
import grails.transaction.Transactional

@Transactional
class AwsService {

	def grailsApplication

	String createDataSource(String path, String dataSchema) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		S3DataSpec dataSpec = new S3DataSpec().withDataLocationS3('s3://' + bucket() + '/' + path).withDataSchema(dataSchema)
		CreateDataSourceFromS3Request dataSourceRequest = new CreateDataSourceFromS3Request().withDataSpec(dataSpec).withComputeStatistics(true)
		CreateDataSourceFromS3Result result = ml.createDataSourceFromS3(dataSourceRequest)
		return result.getDataSourceId()
	}

	String createMLModel(String dataSourceId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		CreateMLModelRequest mlRequest = new CreateMLModelRequest().withTrainingDataSourceId(dataSourceId).withMLModelType(MLModelType.REGRESSION)
		ml.createMLModel(mlRequest)
	}

	String uploadToS3(File file) {
		AmazonS3Client s3 = new AmazonS3Client()
		String bucket = bucket()
		String path = new Date().format(dateFormat()) + file.name
		s3.putObject(bucket, path, file)
		return path
	}

	String bucket() {
		return grailsApplication.config.aws.bucket
	}

	String dateFormat() {
		return grailsApplication.config.augurworks.datePathFormat
	}
}
