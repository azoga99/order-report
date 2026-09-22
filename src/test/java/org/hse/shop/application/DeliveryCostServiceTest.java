package org.hse.shop.application;

import org.hse.shop.domain.DeliveryMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Тесты тарифов доставки. */
class DeliveryCostServiceTest {

    private final DeliveryCostService service = new DeliveryCostService();

    @ParameterizedTest(name = "{0}, товаров на {1} — доставка {2}")
    @CsvSource({
            "PICKUP,  1000.00,   0",
            "PICKUP,  0,         0",
            "POST,    1000.00,   290.00",
            "POST,    20000.00,  290.00",
            "COURIER, 1000.00,   390.00",
            "COURIER, 4999.99,   390.00",
            "COURIER, 5000.00,   0",
            "COURIER, 12000.00,  0"
    })
    @DisplayName("Считает стоимость доставки по тарифу")
    void calculatesCost(DeliveryMethod method, BigDecimal itemsTotal, BigDecimal expected) {
        assertThat(service.costFor(method, itemsTotal)).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Отклоняет отрицательную сумму товаров")
    void rejectsNegativeTotal() {
        assertThatThrownBy(() -> service.costFor(DeliveryMethod.POST, new BigDecimal("-0.01")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
