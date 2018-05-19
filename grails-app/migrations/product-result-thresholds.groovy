databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1523230662154-9") {
		addColumn(tableName: "product") {
			column(name: "real_time_diff_lower", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1523230662154-10") {
		addColumn(tableName: "product") {
			column(name: "real_time_diff_upper", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1523230662154-13") {
		addColumn(tableName: "product") {
			column(name: "volatile_percent_limit", type: "double precision")
		}
	}
}