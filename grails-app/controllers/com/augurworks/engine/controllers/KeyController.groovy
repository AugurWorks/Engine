package com.augurworks.engine.controllers

import com.augurworks.engine.domains.ApiKey
import com.augurworks.engine.domains.Product
import grails.converters.JSON

class KeyController {

    def index() {
        [keys: ApiKey.list(), products: Product.list()]
    }

    def create(String name) {
        ApiKey key = new ApiKey(name: name)
        key.save()
        if (key.hasErrors()) {
            render([ok: false, error: key.errors] as JSON)
        } else {
            render(template: '/key/keyRow', model: [key: key, products: Product.list()])
        }
    }

    def save(Long id) {
        ApiKey key = ApiKey.get(id)
        key.products = JSON.parse(params.products).collect { product -> Product.get(product) }
        key.save()
        if (key.hasErrors()) {
            render([ok: false, error: key.errors] as JSON)
        } else {
            render([ok: true] as JSON)
        }
    }

    def delete(Long id) {
        ApiKey key = ApiKey.get(id)
        key.delete()
        render([ok: true] as JSON)
    }
}
