package org.hse.shop.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hse.shop.domain.Product;

import java.math.BigDecimal;

/** Строка таблицы товаров */
@Entity
@Table(name = "products")
class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    protected ProductEntity() {
    }

    static ProductEntity from(Product product) {
        ProductEntity entity = new ProductEntity();
        entity.id = product.id();
        entity.name = product.name();
        entity.price = product.price();
        entity.stock = product.stock();
        return entity;
    }

    Product toDomain() {
        return new Product(id, name, price, stock);
    }
}
