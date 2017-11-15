databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1510517813942-1") {
        addColumn(tableName: "algorithm_request") {
            column(name: "lower_percent_threshold", type: "double precision")
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1510517813942-2") {
        addColumn(tableName: "algorithm_request") {
            column(name: "upper_percent_threshold", type: "double precision")
        }
    }
}