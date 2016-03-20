import com.augurworks.engine.helper.Aggregation

databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1458481230000-1") {
		grailsChange{
			change{
				Aggregation.values().each { Aggregation aggregation ->
					String update = 'update request_data_set r set r.aggregation=:aggregation where r.aggregation=:oldAggregation'
					sql.executeUpdate(update, [aggregation: aggregation.toString(), oldAggregation: aggregation.name])
				}
			}
		}
	}
}
