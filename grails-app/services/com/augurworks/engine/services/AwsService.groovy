package com.augurworks.engine.services

import com.amazonaws.services.machinelearning.AmazonMachineLearningClient
import com.amazonaws.services.machinelearning.model.*
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3Object
import com.augurworks.engine.helper.Global
import grails.core.GrailsApplication
import grails.transaction.Transactional

import java.util.zip.GZIPInputStream

@Transactional
class AwsService {

	static final String BATCH_PREDICTION_URI = 'predictions'

	GrailsApplication grailsApplication

	String createDataSource(String path, String dataSchema, boolean computeStatistics) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		S3DataSpec dataSpec = new S3DataSpec().withDataLocationS3('s3://' + bucket() + '/' + path).withDataSchema(dataSchema)
		CreateDataSourceFromS3Request dataSourceRequest = new CreateDataSourceFromS3Request().withDataSpec(dataSpec).withComputeStatistics(computeStatistics)
		CreateDataSourceFromS3Result result = ml.createDataSourceFromS3(dataSourceRequest)
		return result.getDataSourceId()
	}

	String createMLModel(String dataSourceId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		CreateMLModelRequest mlRequest = new CreateMLModelRequest().withTrainingDataSourceId(dataSourceId).withMLModelType(MLModelType.REGRESSION)
		CreateMLModelResult mlResult= ml.createMLModel(mlRequest)
		return mlResult.getMLModelId()
	}

	GetMLModelResult getMLModel(String modelId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		GetMLModelRequest mlModelRequest = new GetMLModelRequest().withMLModelId(modelId)
		return ml.getMLModel(mlModelRequest)
	}

	String createBatchPrediction(String dataSourceId, String modelId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		String outputUri = 's3://' + bucket() + '/' + BATCH_PREDICTION_URI
		CreateBatchPredictionRequest batchPredictionRequest = new CreateBatchPredictionRequest().withBatchPredictionDataSourceId(dataSourceId).withMLModelId(modelId).withOutputUri(outputUri)
		CreateBatchPredictionResult batchPredictionResult = ml.createBatchPrediction(batchPredictionRequest)
		return batchPredictionResult.getBatchPredictionId()
	}

	GetBatchPredictionResult getBatchPrediction(String batchPredictionId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		GetBatchPredictionRequest batchPredictionRequest = new GetBatchPredictionRequest().withBatchPredictionId(batchPredictionId)
		return ml.getBatchPrediction(batchPredictionRequest)
	}

	File getBatchPredictionResults(String batchPredictionId) {
		GetBatchPredictionResult batchPredictionResult = getBatchPrediction(batchPredictionId)
		String path = getBatchPredictionUri(batchPredictionResult)
		File zippedResults = downloadFromS3(path)
		File unzippedResults = unzipFile(zippedResults)
		zippedResults.delete()
		return unzippedResults
	}

	String uploadToS3(File file) {
		AmazonS3Client s3 = new AmazonS3Client()
		String bucket = bucket()
		String path = new Date().format(Global.S3_DATE_FORMAT) + file.name
		s3.putObject(bucket, path, file)
		return path
	}

	String uploadToS3(String prefix, String fileName, String content) {
		AmazonS3Client s3 = new AmazonS3Client()
		String bucket = bucket()
		String path = prefix + '/' + new Date().format(Global.S3_DATE_FORMAT) + fileName
		s3.putObject(bucket, path, content)
		return path
	}

	File downloadFromS3(String path) {
		AmazonS3Client s3 = new AmazonS3Client()
		String bucket = bucket()
		S3Object object = s3.getObject(bucket, path)
		File file = File.createTempFile('ZippedFile', '.csv.gz')
		InputStream input = object.getObjectContent()
		byte[] buf = new byte[1024]
		OutputStream out = new FileOutputStream(file)
		int count = 0
		while((count = input.read(buf)) != -1) {
			if(Thread.interrupted()) {
				throw new InterruptedException()
			}
			out.write(buf, 0, count)
		}
		out.close()
		input.close()
		return file
	}

	void deleteFromS3(String path) {
		AmazonS3Client s3 = new AmazonS3Client()
		String bucket = bucket()
		s3.deleteObject(bucket, path)
	}

	void deleteDatasource(String dataSourceId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		DeleteDataSourceRequest deleteRequest = new DeleteDataSourceRequest().withDataSourceId(dataSourceId)
		ml.deleteDataSource(deleteRequest)
	}

	void deleteModel(String modelId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		DeleteMLModelRequest deleteRequest = new DeleteMLModelRequest().withMLModelId(modelId)
		ml.deleteMLModel(deleteRequest)
	}

	void deleteBatchPrediction(String batchPredictionId) {
		AmazonMachineLearningClient ml = new AmazonMachineLearningClient()
		DeleteBatchPredictionRequest deleteRequest = new DeleteBatchPredictionRequest().withBatchPredictionId(batchPredictionId)
		ml.deleteBatchPrediction(deleteRequest)
	}

	String bucket() {
		return grailsApplication.config.aws.bucket
	}

	String getBatchPredictionUri(GetBatchPredictionResult batchPredictionResult) {
		String dataSourceName = batchPredictionResult.getInputDataLocationS3().split('/').last()
		return BATCH_PREDICTION_URI + '/batch-prediction/result/' + batchPredictionResult.getBatchPredictionId() + '-' + dataSourceName + '.gz'
	}

	File unzipFile(File zippedFile) {
		File unzippedFile = File.createTempFile('UnzippedFile', '.csv')
		byte[] buffer = new byte[1024]
		try {
			GZIPInputStream gzis =  new GZIPInputStream(new FileInputStream(zippedFile))
			FileOutputStream out = new FileOutputStream(unzippedFile)
			int len
			while ((len = gzis.read(buffer)) > 0) {
				out.write(buffer, 0, len)
			}
			gzis.close()
			out.close()
		} catch(IOException e){
			log.error e
		}
		return unzippedFile
	}
}
