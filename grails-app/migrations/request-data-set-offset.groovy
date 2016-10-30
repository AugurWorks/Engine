databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1477869152000-1") {
		renameColumn(tableName: "request_data_set", oldColumnName: "offset", newColumnName: "data_offset", columnDataType: "int(11)")
		addNotNullConstraint(tableName: "request_data_set", columnName: "data_offset", columnDataType: "int(11)")
	}
}
