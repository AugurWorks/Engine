databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1456969083293-1") {
		modifyDataType(columnName: "unit", tableName: "algorithm_request", newDataType: "VARCHAR(20)")
		addNotNullConstraint(tableName: "algorithm_request", columnName: "unit", columnDataType: "VARCHAR(20)")
	}
}
