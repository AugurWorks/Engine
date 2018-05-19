databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1525865329575-1") {
		dropForeignKeyConstraint(baseTableName: "algorithm_result", constraintName: "FKpopk1hnis36k2qifo314mk917")
	}

	changeSet(author: "TheConnMan (generated)", id: "1525863592470-2") {
		dropColumn(columnName: "previous_algorithm_result_id", tableName: "algorithm_result")
	}
}
