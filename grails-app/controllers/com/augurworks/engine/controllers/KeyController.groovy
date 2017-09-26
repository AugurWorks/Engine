package com.augurworks.engine.controllers

import com.augurworks.engine.domains.ApiKey
import com.augurworks.engine.domains.Product
import grails.converters.JSON

class KeyController {

    def index() {
        [keys: ApiKey.list(), products: Product.list()]
    }

    def create(String name) {
        try {
            ApiKey key = new ApiKey(name: name)
            key.save()
            if (key.hasErrors()) {
                throw new RuntimeException(key.errors)
            }
            render(template: '/key/keyRow', model: [key: key, products: Product.list()])
        } catch (Exception e) {
            log.error('Error creating an API key', e)
            render([ok: false, error: e.getMessage()] as JSON)
        }
    }

    def save(Long id) {
        try {
            ApiKey key = ApiKey.get(id)
            key.products = JSON.parse(params.products).collect { product -> Product.get(product) }
            key.save()
            if (key.hasErrors()) {
                throw new RuntimeException(key.errors)
            }
            render([ok: true] as JSON)
        } catch (Exception e) {
            log.error('Error creating an API key', e)
            render([ok: false, error: e.getMessage()] as JSON)
        }
    }

    def delete(Long id) {
        try {
            ApiKey key = ApiKey.get(id)
            key.delete()
            render([ok: true] as JSON)
        } catch (Exception e) {
            log.error('Error creating an API key', e)
        }
    }
}
