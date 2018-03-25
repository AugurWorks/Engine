package com.augurworks.engine.domains

class ApiKey {

    String name
    String apiKey = UUID.randomUUID().toString()
    Date lastUsed

    static hasMany = [products: Product]

    List<Product> products

    static constraints = {
        name unique: true
        lastUsed nullable: true
    }

    static mapping = {
        sort 'name'
    }
}
