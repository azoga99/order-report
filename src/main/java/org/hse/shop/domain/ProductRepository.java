package org.hse.shop.domain;

import java.util.List;
import java.util.Optional;

/** Хранилище товаров */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(long id);

    List<Product> findAll();
}
