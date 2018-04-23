databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1524342418044-6") {
		addColumn(tableName: "algorithm_result") {
			column(name: "previous_algorithm_result_id", type: "bigint")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524342418044-13") {
		addForeignKeyConstraint(baseColumnNames: "previous_algorithm_result_id", baseTableName: "algorithm_result", constraintName: "FKpopk1hnis36k2qifo314mk917", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_result")
	}
}