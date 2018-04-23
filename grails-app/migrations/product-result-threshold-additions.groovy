databaseChangeLog = {

	changeSet(author: "TheConnMan (generated)", id: "1524412595697-6") {
		addColumn(tableName: "product") {
			column(name: "is_close_negative_threshold_percent", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524412595697-7") {
		addColumn(tableName: "product") {
			column(name: "is_close_positive_threshold_percent", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524412595697-8") {
		addColumn(tableName: "product") {
			column(name: "is_real_time_negative_threshold_percent", type: "double precision")
		}
	}

	changeSet(author: "TheConnMan (generated)", id: "1524412595697-9") {
		addColumn(tableName: "product") {
			column(name: "is_real_time_positive_threshold_percent", type: "double precision")
		}
	}
}