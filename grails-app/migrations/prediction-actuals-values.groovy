databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1477250676016-1") {
		addColumn(tableName: "predicted_value") {
			column(name: "actual", type: "double precision")
		}
	}
}
