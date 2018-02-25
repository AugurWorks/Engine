databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1516025539068-1") {
		addColumn(tableName: "algorithm_request") {
			column(name: "lower_prediction_percent_threshold", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1516025539068-2") {
		addColumn(tableName: "algorithm_request") {
			column(name: "upper_prediction_percent_threshold", type: "double precision")
		}
	}
}
