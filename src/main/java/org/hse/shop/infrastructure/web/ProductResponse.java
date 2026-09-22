package org.hse.shop.infrastructure.web;

import org.hse.shop.domain.Product;

import java.math.BigDecimal;

public record ProductResponse(long id, String name, BigDecimal price, int stock) {

    static ProductResponse from(Product product) {
        return new ProductResponse(product.id(), product.name(), product.price(), product.stock());
    }
}
