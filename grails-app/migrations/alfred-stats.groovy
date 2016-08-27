databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1469922110032-2") {
		createTable(tableName: "training_stat") {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "training_statPK")
			}

			column(name: "version", type: "bigint") {
				constraints(nullable: "false")
			}

			column(name: "data_sets", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}

			column(name: "learning_constant", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "net_id", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "rms_error", type: "double precision") {
				constraints(nullable: "false")
			}

			column(name: "rounds_trained", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "row_count", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "seconds_elapsed", type: "integer") {
				constraints(nullable: "false")
			}

			column(name: "training_stage", type: "varchar(255)") {
				constraints(nullable: "false")
			}

			column(name: "training_stop_reason", type: "varchar(255)")
		}
	}
}
