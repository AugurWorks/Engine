databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1506685669081-1") {
        addColumn(tableName: "algorithm_result") {
            column(name: "actual_value", type: "double precision")
        }
    }
}