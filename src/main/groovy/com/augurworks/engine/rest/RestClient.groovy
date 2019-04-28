package com.augurworks.engine.rest

import com.augurworks.engine.data.SingleDataRequest
import com.augurworks.engine.helper.TradingHours
import com.augurworks.engine.helper.Unit
import com.augurworks.engine.instrumentation.Instrumentation
import com.augurworks.engine.model.DataSetValue
import com.timgroup.statsd.StatsDClient
import grails.util.Holders

import com.amazonaws.services.s3.AmazonS3Client
import com.augurworks.engine.helper.Global
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class RestClient implements ApiClient {

	private static final Logger log = LoggerFactory.getLogger(RestClient)

	private static final String BUCKET = Holders.config.aws.bucket
	private static final boolean LOGGING_ON = Holders.config.logging.files

	protected final StatsDClient statsdClient = Instrumentation.statsdClient

	protected void logStringToS3(String filename, String text) {
		try {
			if (LOGGING_ON) {
				long startTime = System.currentTimeMillis()
				statsdClient.increment('count.data.s3.uploads')
				File file = File.createTempFile(filename, '.txt')
				file.write(text)
				AmazonS3Client s3 = new AmazonS3Client()
				Date date = new Date()
				String path = 'logs/' + date.format(Global.S3_DATE_FORMAT) + filename + '.txt'
				s3.putObject(BUCKET, path, file)
				file.delete()
				statsdClient.recordGaugeValue('histogram.data.s3.upload.time', System.currentTimeMillis() - startTime, 'un:ms')
			}
		} catch (e) {
			log.warn(e.getMessage(), e)
		}
	}

	Collection<DataSetValue> getHistory(SingleDataRequest dataRequest) {
		return getHistory(dataRequest, false) + getHistory(dataRequest, true)
	}

	abstract Collection<DataSetValue> getHistory(SingleDataRequest dataRequest, boolean getNowOnly)

	protected Date getOffsetStartDate(SingleDataRequest dataRequest, Boolean getNowOnly) {
		if (getNowOnly) {
			return TradingHours.floorPeriod(new Date(), dataRequest.unit == Unit.DAY ? 1440 : 5)
		}
		return dataRequest.getOffsetStartDate()
	}

	protected Date getOffsetEndDate(SingleDataRequest dataRequest, Boolean getNowOnly) {
		return getNowOnly ? new Date() : dataRequest.getOffsetEndDate()
	}
}
