import com.augurworks.engine.domains.AlgorithmRequest

databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1470509660908-1") {
		addColumn(tableName: "algorithm_request") {
			column(name: "alfred_environment", type: "varchar(255)")
		}

		grailsChange{
			change{
				String updateDays = "update AlgorithmRequest a set a.alfredEnvironment='DOCKER' where a.unit='DAY'"
				AlgorithmRequest.executeUpdate(updateDays)

				String updateIntradays = "update AlgorithmRequest a set a.alfredEnvironment='LAMBDA' where a.unit!='DAY'"
				AlgorithmRequest.executeUpdate(updateIntradays)
			}
		}

		addNotNullConstraint(tableName: "algorithm_request", columnName: "alfred_environment", columnDataType: "varchar(255)")
	}
}
