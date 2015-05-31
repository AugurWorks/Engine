package com.augurworks.engine

import grails.transaction.Transactional

@Transactional
class DataRetrievalService {

	def grailsApplication

	Collection<Collection> getQuandlData(DataSet dataSet) {
		String quandlKey = grailsApplication.config.augurworks.quandl.key;
		String quandlPre = 'https://www.quandl.com/api/v1/datasets/';
		String quandlPost = '.csv?auth_token=' + quandlKey;
		String url = quandlPre + dataSet.code + quandlPost;
		return new URL(url).getText().split('\n').tail().collect { String line ->
			Collection<String> lineValues = line.split(',');
			return [Date.parse('yyyy-MM-dd', lineValues[0]), lineValues[dataSet.dataColumn]];
		}
	}
}
