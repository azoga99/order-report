package org.hse.shop.infrastructure.persistence;

import org.hse.shop.domain.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/** Тесты хранения покупателей в H2. */
@DataJpaTest
@Import(CustomerRepositoryAdapter.class)
class CustomerRepositoryAdapterTest {

    @Autowired
    private CustomerRepositoryAdapter repository;

    @Test
    @DisplayName("Сохраняет покупателя и находит его по id")
    void savesAndFinds() {
        Customer saved = repository.save(Customer.create("Игорь Лебедев", "+79260000001"));

        assertThat(repository.findById(saved.id())).contains(saved);
    }

    @Test
    @DisplayName("Проверяет, занят ли телефон")
    void checksPhone() {
        repository.save(Customer.create("Игорь Лебедев", "+79260000002"));

        assertThat(repository.existsByPhone("+79260000002")).isTrue();
        assertThat(repository.existsByPhone("+79260000003")).isFalse();
    }
}
