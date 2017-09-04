package com.augurworks.engine.controllers

import com.augurworks.engine.domains.Product

class ProductController {

    def index() {
        [products: Product.list()]
    }
}
