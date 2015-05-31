package com.augurworks.engine

import grails.transaction.Transactional

@Transactional
class DataGeneratorService {

	void importQuandlDataSets() {
		new URL('https://s3.amazonaws.com/quandl-static-content/quandl-stock-code-list.csv').getText().split('\n').tail().each { String line ->
			Collection<String> row = line.split(',');
			if (row[2] != 'NA' && row[4] == 'Active') {
				new DataSet(ticker: row[0], name: row[1], code: row[2], dataColumn: 4).save();
			}
		}
	}
}
