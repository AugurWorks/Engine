databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1504536565638-1") {
        createTable(tableName: "product") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "productPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(unique: "true")
            }
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1504536565638-2") {
        addColumn(tableName: "algorithm_request") {
            column(name: "product_id", type: "bigint")
        }
    }
}