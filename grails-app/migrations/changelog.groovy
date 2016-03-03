databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-1") {
		createTable(tableName: "algorithm_request") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "dependant_data_set_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "end_offset", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "start_offset", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "unit", type: "VARCHAR(4)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-2") {
		createTable(tableName: "algorithm_result") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "alfred_model_id", type: "VARCHAR(255)")

			column(name: "algorithm_request_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "complete", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "end_date", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "machine_learning_model_id", type: "BIGINT")

			column(name: "model_type", type: "VARCHAR(16)") {
				constraints(nullable: "false")
			}

			column(name: "start_date", type: "DATETIME") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-3") {
		createTable(tableName: "data_set") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "code", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "data_column", type: "INT") {
				constraints(nullable: "false")
			}

			column(name: "name", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "ticker", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-4") {
		createTable(tableName: "machine_learning_model") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "batch_prediction_id", type: "VARCHAR(255)")

			column(name: "model_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "prediction_data_source_id", type: "VARCHAR(255)")

			column(name: "training_data_source_id", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-5") {
		createTable(tableName: "predicted_value") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "algorithm_result_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "date", type: "DATETIME") {
				constraints(nullable: "false")
			}

			column(name: "value", type: "DOUBLE") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-6") {
		createTable(tableName: "request_data_set") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "aggregation", type: "VARCHAR(21)") {
				constraints(nullable: "false")
			}

			column(name: "algorithm_request_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "data_set_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "offset", type: "INT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-7") {
		createTable(tableName: "role") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "authority", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-8") {
		createTable(tableName: "user") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}

			column(name: "version", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "account_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "account_locked", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "avatar_url", type: "VARCHAR(255)")

			column(name: "enabled", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "password", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}

			column(name: "password_expired", type: "BIT") {
				constraints(nullable: "false")
			}

			column(name: "username", type: "VARCHAR(255)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-9") {
		createTable(tableName: "user_role") {
			column(name: "role_id", type: "BIGINT") {
				constraints(nullable: "false")
			}

			column(name: "user_id", type: "BIGINT") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-10") {
		addPrimaryKey(columnNames: "role_id, user_id", tableName: "user_role")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-19") {
		createIndex(indexName: "UK_qsahlm4auqgjdxy0k4qqv74oc", tableName: "data_set", unique: "true") {
			column(name: "name")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-20") {
		createIndex(indexName: "UK_irsamgnera6angm0prq1kemt2", tableName: "role", unique: "true") {
			column(name: "authority")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-21") {
		createIndex(indexName: "UK_sb8bbouer5wak8vyiiy4pf2bx", tableName: "user", unique: "true") {
			column(name: "username")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-11") {
		addForeignKeyConstraint(baseColumnNames: "dependant_data_set_id", baseTableName: "algorithm_request", baseTableSchemaName: "engine", constraintName: "FK_5vh8j9pc6pb65v5u5okrc3d", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "data_set", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-12") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_request_id", baseTableName: "algorithm_result", baseTableSchemaName: "engine", constraintName: "FK_ju10bk16m22xkb3l4cgch54eb", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "algorithm_request", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-13") {
		addForeignKeyConstraint(baseColumnNames: "machine_learning_model_id", baseTableName: "algorithm_result", baseTableSchemaName: "engine", constraintName: "FK_lpq95003culfbjr8631x685xg", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "machine_learning_model", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-14") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_result_id", baseTableName: "predicted_value", baseTableSchemaName: "engine", constraintName: "FK_b19ndwm4s0wfw525je79qd46l", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "algorithm_result", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-15") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_request_id", baseTableName: "request_data_set", baseTableSchemaName: "engine", constraintName: "FK_cooqrowbpg2g86o80pu790ptb", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "algorithm_request", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-16") {
		addForeignKeyConstraint(baseColumnNames: "data_set_id", baseTableName: "request_data_set", baseTableSchemaName: "engine", constraintName: "FK_j82t2mgnxmtcb86yqcpl7nbhj", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "data_set", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-17") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", baseTableSchemaName: "engine", constraintName: "FK_it77eq964jhfqtu54081ebtio", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "role", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}

	changeSet(author: "TheConnMan (generated)", id: "1456965619978-18") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", baseTableSchemaName: "engine", constraintName: "FK_apcc8lxk2xnug8377fatvbn04", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "id", referencedTableName: "user", referencedTableSchemaName: "engine", referencesUniqueColumn: "false")
	}
}
