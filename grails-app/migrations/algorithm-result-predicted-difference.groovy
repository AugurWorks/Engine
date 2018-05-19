databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1524342629356-5") {
		addColumn(tableName: "algorithm_result") {
			column(name: "predicted_difference", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524342629356-6") {
		addColumn(tableName: "predicted_value") {
			column(name: "predicted_values_idx", type: "integer")
		}
	}
}