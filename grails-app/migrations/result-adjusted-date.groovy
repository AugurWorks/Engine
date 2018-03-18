databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1521385711612-1") {
		addColumn(tableName: "algorithm_result") {
			column(name: "adjusted_date_created", type: "datetime")
		}
	}
}
