import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.RequestDataSet

databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1460309334837-1") {
		addColumn(tableName: "algorithm_request") {
			column(name: "dependant_symbol", type: "varchar(255)")
		}

		grailsChange{
			change{
				String update = 'update algorithm_request a inner join data_set d on a.dependant_data_set_id = d.id set a.dependant_symbol=d.ticker'
				sql.executeUpdate(update)
			}
		}

		addNotNullConstraint(tableName: "algorithm_request", columnName: "dependant_symbol", columnDataType: "varchar(255)")
	}

	changeSet(author: "TheConnMan (generated)", id: "1460309334837-2") {
		addColumn(tableName: "request_data_set") {
			column(name: "datasource", type: "varchar(255)")
		}

		grailsChange{
			change{
				String update = 'update request_data_set r set r.datasource=:datasource'
				sql.executeUpdate(update, [datasource: 'TD'])
			}
		}

		addNotNullConstraint(tableName: "request_data_set", columnName: "datasource", columnDataType: "varchar(255)")
	}

	changeSet(author: "TheConnMan (generated)", id: "1460309334837-3") {
		addColumn(tableName: "request_data_set") {
			column(name: "name", type: "varchar(255)")
		}

		grailsChange{
			change {
				String update = 'update request_data_set r inner join data_set d on r.data_set_id = d.id set r.name=d.ticker'
				sql.executeUpdate(update)
			}
		}

		addNotNullConstraint(tableName: "request_data_set", columnName: "name", columnDataType: "varchar(255)")
	}

	changeSet(author: "TheConnMan (generated)", id: "1460309334837-4") {
		addColumn(tableName: "request_data_set") {
			column(name: "symbol", type: "varchar(255)")
		}

		grailsChange{
			change{
				String update = 'update request_data_set r inner join data_set d on r.data_set_id = d.id set r.symbol=d.ticker'
				sql.executeUpdate(update)
			}
		}

		addNotNullConstraint(tableName: "request_data_set", columnName: "symbol", columnDataType: "varchar(255)")
	}

	changeSet(author: "TheConnMan (generated)", id: "1460309334837-5") {
		dropColumn(columnName: "dependant_data_set_id", tableName: "algorithm_request")
	}

	changeSet(author: "TheConnMan (generated)", id: "1460309334837-6") {
		dropColumn(columnName: "data_set_id", tableName: "request_data_set")
	}

	changeSet(author: "TheConnMan (generated)", id: "1460309334837-7") {
		dropTable(tableName: "data_set")
	}
}
