databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1458402486289-1") {
		addColumn(tableName: "algorithm_request") {
			column(name: "cron_expression", type: "varchar(255)")
		}
	}
}
