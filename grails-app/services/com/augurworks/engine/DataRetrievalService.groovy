package com.augurworks.engine

import grails.transaction.Transactional

@Transactional
class DataRetrievalService {

	def grailsApplication

	Collection getDataSetValues(DataSet dataSet, Date startDate, Date endDate, int offset) {
		Collection rawData = getQuandlData(dataSet.code, dataSet.dataColumn);
		int startIndex = rawData.findIndexOf { it[0] == startDate };
		int endIndex = rawData.findIndexOf { it[0] == endDate };
		if (startIndex != -1 && endIndex != -1 && startIndex + offset >= 0 && endIndex <= rawData.size()) {
			return rawData[(startIndex + offset)..(endIndex + offset)];
		} else {
			String dateFormat = grailsApplication.config.augurworks.dateFormat;
			log.warn 'Invalid date range, ' + startDate.format(dateFormat) + '-' + endDate.format(dateFormat)
			return [];
		}
	}

	Collection getQuandlData(String quandlCode, int dataColumn) {
		String quandlKey = grailsApplication.config.augurworks.quandl.key;
		String quandlPre = 'https://www.quandl.com/api/v1/datasets/';
		String quandlPost = '.csv?auth_token=' + quandlKey;
		String url = quandlPre + quandlCode + quandlPost;
		return new URL(url).getText().split('\n').tail().reverse().collect { String line ->
			Collection<String> lineValues = line.split(',');
			return [Date.parse('yyyy-MM-dd', lineValues[0]), lineValues[dataColumn]];
		}
	}
}
