databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1482092182029-1") {
        createTable(tableName: "algorithm_request_tags") {
            column(name: "algorithm_request_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tags_string", type: "VARCHAR(255)")
        }
    }
}