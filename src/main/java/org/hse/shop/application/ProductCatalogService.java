package org.hse.shop.application;

import org.hse.shop.domain.NotFoundException;
import org.hse.shop.domain.Product;
import org.hse.shop.domain.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** Каталог товаров: добавление, просмотр и смена цены */
@Service
public class ProductCatalogService {

    private final ProductRepository products;

    public ProductCatalogService(ProductRepository products) {
        this.products = products;
    }

    /** Новый товар попадает в каталог без остатка, склад пополняется отдельно */
    public Product add(String name, BigDecimal price) {
        return products.save(Product.create(name, price));
    }

    public Product get(long id) {
        return products.findById(id).orElseThrow(() -> NotFoundException.product(id));
    }

    public List<Product> list() {
        return products.findAll();
    }

    /** Товары, которые сейчас есть на складе */
    public List<Product> inStock() {
        return products.findAll().stream()
                .filter(product -> product.stock() > 0)
                .toList();
    }

    public Product changePrice(long id, BigDecimal newPrice) {
        return products.save(get(id).withPrice(newPrice));
    }
}
