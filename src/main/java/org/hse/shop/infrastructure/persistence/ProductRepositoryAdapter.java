package org.hse.shop.infrastructure.persistence;

import org.hse.shop.domain.Product;
import org.hse.shop.domain.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Хранилище товаров поверх Spring Data JPA */
@Repository
public class ProductRepositoryAdapter implements ProductRepository {

    private final ProductJpaRepository jpa;

    ProductRepositoryAdapter(ProductJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Product save(Product product) {
        return jpa.save(ProductEntity.from(product)).toDomain();
    }

    @Override
    public Optional<Product> findById(long id) {
        return jpa.findById(id).map(ProductEntity::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return jpa.findAll(Sort.by("id")).stream().map(ProductEntity::toDomain).toList();
    }
}
