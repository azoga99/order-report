package org.hse.shop.infrastructure.persistence;

import org.hse.shop.domain.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Тесты хранения товаров в H2. */
@DataJpaTest
@Import(ProductRepositoryAdapter.class)
class ProductRepositoryAdapterTest {

    @Autowired
    private ProductRepositoryAdapter repository;

    @Test
    @DisplayName("Назначает id новому товару")
    void assignsId() {
        Product saved = repository.save(Product.create("Турка", new BigDecimal("990.00")));

        assertThat(saved.id()).isNotNull();
        assertThat(repository.findById(saved.id())).contains(saved);
    }

    @Test
    @DisplayName("Повторное сохранение обновляет товар, а не создаёт новый")
    void updatesExisting() {
        Product saved = repository.save(Product.create("Турка", new BigDecimal("990.00")));
        int countBefore = repository.findAll().size();

        repository.save(saved.receive(4));

        assertThat(repository.findById(saved.id())).get().extracting(Product::stock).isEqualTo(4);
        assertThat(repository.findAll()).hasSize(countBefore);
    }

    @Test
    @DisplayName("Возвращает товары по порядку id")
    void findsAllSortedById() {
        repository.save(Product.create("Турка", new BigDecimal("990.00")));
        repository.save(Product.create("Сахар", new BigDecimal("99.00")));

        assertThat(repository.findAll()).extracting(Product::id).isSorted();
    }
}
