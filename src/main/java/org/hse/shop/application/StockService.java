package org.hse.shop.application;

import org.hse.shop.domain.Product;
import org.hse.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;

/** Склад: поступление товара, списание под заказ и возврат */
@Service
public class StockService {

    private final ProductCatalogService catalog;
    private final ProductRepository products;

    public StockService(ProductCatalogService catalog, ProductRepository products) {
        this.catalog = catalog;
        this.products = products;
    }

    /** Поступление товара от поставщика */
    public Product receive(long productId, int quantity) {
        return products.save(catalog.get(productId).receive(quantity));
    }

    /** Списание под заказ. Если остатка не хватает, бросает BusinessRuleException */
    public Product reserve(long productId, int quantity) {
        return products.save(catalog.get(productId).take(quantity));
    }

    /** Возврат товара на склад после отмены заказа */
    public Product release(long productId, int quantity) {
        return products.save(catalog.get(productId).receive(quantity));
    }
}
