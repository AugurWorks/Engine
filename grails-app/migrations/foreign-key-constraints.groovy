databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-1") {
		addPrimaryKey(columnNames: "algorithm_request_id, algorithm_type", tableName: "algorithm_request_cron_algorithms")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-2") {
		addUniqueConstraint(columnNames: "name", constraintName: "UC_API_KEYNAME_COL", tableName: "api_key")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-3") {
		addForeignKeyConstraint(baseColumnNames: "api_key_products_id", baseTableName: "api_key_product", constraintName: "FK13kdt1o1i2k54bhbwoo9lamdt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "api_key")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-4") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_request_id", baseTableName: "algorithm_request_cron_algorithms", constraintName: "FK470wi8kfdfaihr4l1qotc9v97", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-5") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_result_id", baseTableName: "predicted_value", constraintName: "FK4bo119hax5h0yfib2josukt0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_result")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-6") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_request_id", baseTableName: "request_data_set", constraintName: "FK51l57d87ia9ilsmwsnovfncw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-7") {
		addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FK859n2jvi8ivhui0rl0esws6o", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "user")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-8") {
		addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FKa68196081fvovjhkek5m97n3y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-9") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_request_id", baseTableName: "request_tag", constraintName: "FKc8b5jtjeojgulfw3q8ki6bgg1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-10") {
		addForeignKeyConstraint(baseColumnNames: "algorithm_request_id", baseTableName: "algorithm_result", constraintName: "FKj4x97u6kyrv5ymw6bjbodxel4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-11") {
		addForeignKeyConstraint(baseColumnNames: "machine_learning_model_id", baseTableName: "algorithm_result", constraintName: "FKknd9a6fmnvnhopnni2fch2k7g", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "machine_learning_model")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-12") {
		addForeignKeyConstraint(baseColumnNames: "product_id", baseTableName: "api_key_product", constraintName: "FKn9hskjysfw6fd8rccksluqyak", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "product")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-14") {
		dropTable(tableName: "registration_code")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-15") {
		addNotNullConstraint(columnDataType: "bigint", columnName: "algorithm_request_id", tableName: "algorithm_request_cron_algorithms")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-16") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "algorithm_type", tableName: "algorithm_request_cron_algorithms")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-17") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "api_key", tableName: "api_key")
	}

	changeSet(author: "TheConnMan (generated)", id: "1509227665879-18") {
		addNotNullConstraint(columnDataType: "varchar(255)", columnName: "name", tableName: "product")
	}
}
