databaseChangeLog = {

	changeSet(author: "TheConnMan", id: "1466954362983-1") {
		grailsChange{
			change{
				String update = 'update algorithm_request a set a.dependant_symbol=CONCAT(a.dependant_symbol, " - CLOSE")'
				sql.executeUpdate(update)
			}
		}
	}
}
