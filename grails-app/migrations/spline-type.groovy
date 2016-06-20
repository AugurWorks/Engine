databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1466370005877-1") {
		addColumn(tableName: "algorithm_request") {
			column(name: "spline_type", type: "varchar(255)")
		}
		
		grailsChange{
			change{
				String update = 'update algorithm_request a set a.spline_type="FILL"'
				sql.executeUpdate(update)
			}
		}
		
		addNotNullConstraint(tableName: "algorithm_request", columnName: "spline_type", columnDataType: "varchar(255)")
	}
}
