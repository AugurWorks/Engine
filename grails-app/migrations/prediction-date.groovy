databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1507481218222-1") {
		addColumn(tableName: "algorithm_result") {
			column(name: "predicted_date", type: "datetime")
		}
	}
}