databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1486319835292-1") {
        addColumn(tableName: "algorithm_request") {
            column(name: "depth", type: "integer")
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1486319835292-2") {
        addColumn(tableName: "algorithm_request") {
            column(name: "learning_constant", type: "double precision")
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1486319835292-3") {
        addColumn(tableName: "algorithm_request") {
            column(name: "training_rounds", type: "integer")
        }
    }
}
