package com.augurworks.engine.services

import com.amazonaws.services.s3.AmazonS3Client
import grails.transaction.Transactional

@Transactional
class AwsService {

	def grailsApplication

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
