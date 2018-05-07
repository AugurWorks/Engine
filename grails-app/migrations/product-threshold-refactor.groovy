databaseChangeLog = {

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-1") {
        addColumn(tableName: "product") {
            column(name: "close_change_threshold", type: "double precision")
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-2") {
        addColumn(tableName: "product") {
            column(name: "close_diff_threshold", type: "double precision")
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-3") {
        addColumn(tableName: "product") {
            column(name: "real_time_change_threshold", type: "double precision")
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-4") {
        addColumn(tableName: "product") {
            column(name: "real_time_diff_threshold", type: "double precision")
        }
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-6") {
        dropColumn(columnName: "is_close_negative_threshold_percent", tableName: "product")
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-7") {
        dropColumn(columnName: "is_close_positive_threshold_percent", tableName: "product")
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-8") {
        dropColumn(columnName: "is_real_time_negative_threshold_percent", tableName: "product")
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-9") {
        dropColumn(columnName: "is_real_time_positive_threshold_percent", tableName: "product")
    }

    changeSet(author: "TheConnMan (generated)", id: "1525729483111-10") {
        dropColumn(columnName: "predicted_values_idx", tableName: "predicted_value")
    }
}