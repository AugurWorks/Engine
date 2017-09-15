package com.augurworks.engine.controllers

import com.augurworks.engine.domains.ApiKey
import com.augurworks.engine.domains.Product
import grails.converters.JSON

class KeyController {

    def index() {
        [keys: ApiKey.list(), products: Product.list()]
    }

    def delete(Long id) {
        ApiKey key = ApiKey.get(id)
        key.delete()
        render([ok: true] as JSON)
    }
}
