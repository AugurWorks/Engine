databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1505490801647-1") {
        createTable(tableName: "api_key") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "api_keyPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "key", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_used", type: "datetime")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1505490801647-2") {
        createTable(tableName: "api_key_product") {
            column(name: "api_key_products_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "product_id", type: "BIGINT")
        }
    }
}