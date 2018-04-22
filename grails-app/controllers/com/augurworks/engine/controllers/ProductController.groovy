package com.augurworks.engine.controllers

import com.augurworks.engine.domains.AlgorithmRequest
import com.augurworks.engine.domains.Product
import grails.converters.JSON

class ProductController {

    def index() {
        [products: Product.list()]
    }

    def create(String name, Double volatilePercentLimit, Double diffUpperThreshold, Double diffLowerThreshold) {
        Product product = new Product(
                name: name,
                volatilePercentLimit: volatilePercentLimit,
                diffUpperThreshold: diffUpperThreshold,
                diffLowerThreshold: diffLowerThreshold)
        product.save()
        if (product.hasErrors()) {
            render([ok: false, error: product.errors] as JSON)
        } else {
            render(template: '/product/productRow', model: [product: product])
        }
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
