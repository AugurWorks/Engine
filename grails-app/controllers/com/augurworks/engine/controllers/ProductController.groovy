package com.augurworks.engine.controllers

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.Product
import grails.converters.JSON

class ProductController {

    def index() {
        [products: Product.list()]
    }

    def delete(Long id) {
        Product product = Product.get(id)
        AlgorithmRequest.findAllByProduct(product).each { request ->
            request.product = null
            request.save()
        }
        product.delete()
        render([ok: true] as JSON)
    }
}
