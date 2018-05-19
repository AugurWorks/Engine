databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1524411905620-4") {
		addColumn(tableName: "product") {
			column(name: "diff_lower_threshold", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524411905620-5") {
		addColumn(tableName: "product") {
			column(name: "diff_upper_threshold", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524411905620-12") {
		dropColumn(columnName: "real_time_diff_lower", tableName: "product")
	}

	changeSet(author: "TheConnMan (generated)", id: "1524411905620-13") {
		dropColumn(columnName: "real_time_diff_upper", tableName: "product")
	}
}