databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1525732434616-10") {
		dropColumn(columnName: "lower_percent_threshold", tableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1525732434616-11") {
		dropColumn(columnName: "lower_prediction_percent_threshold", tableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1525732434616-13") {
		dropColumn(columnName: "upper_percent_threshold", tableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1525732434616-14") {
		dropColumn(columnName: "upper_prediction_percent_threshold", tableName: "algorithm_request")
	}
}