package org.hse.shop.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Тесты товара: валидация и движение остатка. */
class ProductTest {

    private final Product kettle = new Product(1L, "Чайник", new BigDecimal("2490.00"), 5);

    @Test
    @DisplayName("Новый товар создаётся без id и без остатка")
    void createsWithoutStock() {
        Product product = Product.create("Чайник", new BigDecimal("2490.00"));

        assertThat(product.id()).isNull();
        assertThat(product.stock()).isZero();
    }

    @Test
    @DisplayName("Отклоняет пустое название")
    void rejectsBlankName() {
        assertThatThrownBy(() -> Product.create(" ", BigDecimal.TEN))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Название");
    }

    @Test
    @DisplayName("Отклоняет нулевую цену (граничный случай)")
    void rejectsZeroPrice() {
        assertThatThrownBy(() -> Product.create("Чайник", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Цена");
    }

    @Test
    @DisplayName("Отклоняет отрицательный остаток")
    void rejectsNegativeStock() {
        assertThatThrownBy(() -> new Product(1L, "Чайник", BigDecimal.TEN, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Меняет цену, остальное не трогает")
    void changesPrice() {
        Product changed = kettle.withPrice(new BigDecimal("1990.00"));

        assertThat(changed.price()).isEqualByComparingTo("1990.00");
        assertThat(changed.stock()).isEqualTo(5);
        assertThat(changed.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Поступление увеличивает остаток")
    void receiveAddsStock() {
        assertThat(kettle.receive(3).stock()).isEqualTo(8);
    }

    @Test
    @DisplayName("Списание уменьшает остаток")
    void takeReducesStock() {
        assertThat(kettle.take(2).stock()).isEqualTo(3);
    }

    @Test
    @DisplayName("Можно списать весь остаток (граничный случай)")
    void takesWholeStock() {
        assertThat(kettle.take(5).stock()).isZero();
    }

    @Test
    @DisplayName("Нельзя списать больше, чем есть на складе")
    void cannotTakeMoreThanStock() {
        assertThatThrownBy(() -> kettle.take(6))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("на складе 5, нужно 6");
    }

    @Test
    @DisplayName("Количество при поступлении и списании должно быть положительным")
    void quantityMustBePositive() {
        assertThatThrownBy(() -> kettle.receive(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kettle.take(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
