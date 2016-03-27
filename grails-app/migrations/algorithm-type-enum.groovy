import com.augurworks.engine.helper.AlgorithmType

databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1457736544180-1") {
		grailsChange{
			change{
				AlgorithmType.values().each { AlgorithmType algorithmType ->
					String update = 'update algorithm_result a set a.model_type=:modelType where a.model_type=:oldModelType'
					sql.executeUpdate(update, [modelType: algorithmType.toString(), oldModelType: algorithmType.name])
				}
			}
		}
	}
}
