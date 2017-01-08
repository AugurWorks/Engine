databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1483901096925-1") {
        addColumn(tableName: "algorithm_request") {
            column(name: "slack_channel", type: "varchar(255)")
        }
    }
}