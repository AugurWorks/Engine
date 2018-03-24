databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1524342147790-1") {
		createTable(tableName: "product_result") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(primaryKey: "true", primaryKeyName: "product_resultPK")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "close_result_id", type: "BIGINT")

			column(name: "previous_run_id", type: "BIGINT")

			column(name: "product_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "real_time_result_id", type: "BIGINT")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524342147790-9") {
		addForeignKeyConstraint(baseColumnNames: "close_result_id", baseTableName: "product_result", constraintName: "FK5q6s8hr6lreigpvthrg4dvx92", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_result")
	}

	changeSet(author: "TheConnMan (generated)", id: "1524342147790-10") {
		addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "product_result", constraintName: "FK7e68ly6fvu2wfe88yhy0u5gml", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product")
	}

	changeSet(author: "TheConnMan (generated)", id: "1524342147790-11") {
		addForeignKeyConstraint(baseColumnNames: "previous_run_id", baseTableName: "product_result", constraintName: "FKas63faaycpg5o42s998y1qs45", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product_result")
	}

	changeSet(author: "TheConnMan (generated)", id: "1524342147790-12") {
		addForeignKeyConstraint(baseColumnNames: "real_time_result_id", baseTableName: "product_result", constraintName: "FKrupkbkwg79odailejb1nvwmts", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_result")
	}
}