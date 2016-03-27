import com.augurworks.engine.domains.AlgorithmRequest

databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1457277704732-2") {
		addColumn(tableName: "algorithm_request") {
			column(name: "name", type: "varchar(255)") {
				constraints(unique: "true")
			}
		}

		grailsChange{
			change{
				Collection algorithmRequests = AlgorithmRequest.createCriteria().list {
					projections {
						property('id')
					}
				}
				algorithmRequests.each { Long algorithmRequestId ->
					String update = 'update AlgorithmRequest a set a.name=:name where a.id=:id'
					AlgorithmRequest.executeUpdate(update, [name: 'Request ' + algorithmRequestId, id: algorithmRequestId])
				}
			}
		}

		addNotNullConstraint(tableName: "algorithm_request", columnName: "name", columnDataType: "varchar(255)")
	}
}
