package com.augurworks.engine.domains

class Product {

    String name

    static constraints = {
        name unique: true
    }

    static mapping = {
        sort 'name'
    }
}
