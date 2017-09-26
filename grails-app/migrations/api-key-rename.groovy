databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1506389764696-1") {
        renameColumn(tableName: "api_key", oldColumnName: "key", newColumnName: "api_key", columnDataType: "varchar(255)")
    }
}
