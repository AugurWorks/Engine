databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1466893092439-1") {
		addColumn(tableName: "request_data_set") {
			column(name: "data_type", type: "varchar(255)")
		}

		grailsChange{
			change{
				String update = 'update request_data_set r set r.data_type="CLOSE"'
				sql.executeUpdate(update)
			}
		}

		addNotNullConstraint(tableName: "request_data_set", columnName: "data_type", columnDataType: "varchar(255)")
	}
}
