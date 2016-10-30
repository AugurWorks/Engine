package com.augurworks.engine.rest

import grails.util.Holders

import com.amazonaws.services.s3.AmazonS3Client
import com.augurworks.engine.helper.Global

abstract class RestClient implements ApiClient {

	private static final String BUCKET = Holders.config.aws.bucket
	private static final boolean LOGGING_ON = Holders.config.logging.files

	protected void logStringToS3(String filename, String text) {
		try {
			if (LOGGING_ON) {
				File file = File.createTempFile(filename, '.txt')
				file.write(text)
				AmazonS3Client s3 = new AmazonS3Client()
				Date date = new Date()
				String path = 'logs/' + date.format(Global.S3_DATE_FORMAT) + filename + '.txt'
				s3.putObject(BUCKET, path, file)
				file.delete()
			}
		} catch (e) {
			log.warn e
		}
	}
}
