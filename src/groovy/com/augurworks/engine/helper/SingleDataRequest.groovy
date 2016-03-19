package com.augurworks.engine.helper

import com.augurworks.engine.domains.DataSet

class SingleDataRequest {
	
	DataSet dataSet
	int offset
	Date startDate
	Date endDate
	String unit
	int minOffset
	int maxOffset
	Aggregation aggregation
}
