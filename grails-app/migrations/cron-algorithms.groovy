databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1458486773535-1") {
		createTable(tableName: "algorithm_request_cron_algorithms") {
			column(name: "algorithm_request_id", type: "bigint")

			column(name: "algorithm_type", type: "varchar(255)")
		}
	}
}
