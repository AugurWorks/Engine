import com.augurworks.engine.helper.Unit

databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1459712042042-1") {
		grailsChange{
			change{
				Unit.values().each { Unit unit ->
					String update = 'update algorithm_request a set a.unit=:unit where a.unit=:oldUnit'
					sql.executeUpdate(update, [unit: unit.toString(), oldUnit: unit.name])
				}
			}
		}
	}
}
