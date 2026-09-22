package org.hse.shop.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Тесты валидации покупателя. */
class CustomerTest {

    @Test
    @DisplayName("Создаёт покупателя с корректным телефоном")
    void createsCustomer() {
        Customer customer = Customer.create("Мария Ковалёва", "+79161234567");

        assertThat(customer.id()).isNull();
        assertThat(customer.name()).isEqualTo("Мария Ковалёва");
        assertThat(customer.phone()).isEqualTo("+79161234567");
    }

    @Test
    @DisplayName("Отклоняет пустое имя")
    void rejectsBlankName() {
        assertThatThrownBy(() -> Customer.create("", "+79161234567"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Имя");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"89161234567", "+7916123456", "+791612345678", "+7 916 123-45-67"})
    @DisplayName("Отклоняет телефон не в формате +7XXXXXXXXXX")
    void rejectsWrongPhone(String phone) {
        assertThatThrownBy(() -> Customer.create("Мария", phone))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Телефон");
    }
}
