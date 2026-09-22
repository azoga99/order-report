package org.hse.shop.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты валидации и создания заказа. */
class OrderTest {

    @Test
    @DisplayName("Создаёт заказ с корректными данными")
    void createsValidOrder() {
        Payment payment = new Payment.Sbp("+79161234567");
        Order order = new Order(1, "Иван", new BigDecimal("100.00"), payment);

        assertEquals(1, order.id());
        assertEquals("Иван", order.customer());
        assertEquals(0, order.amount().compareTo(new BigDecimal("100.00")));
        assertSame(payment, order.payment());
    }

    @Test
    @DisplayName("Разрешает нулевую сумму (граничный случай)")
    void allowsZeroAmount() {
        assertDoesNotThrow(() ->
                new Order(2, "Пётр", BigDecimal.ZERO, new Payment.Cash(BigDecimal.ZERO)));
    }

    @Test
    @DisplayName("Отклоняет отрицательную сумму")
    void rejectsNegativeAmount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Order(3, "Анна", new BigDecimal("-0.01"), new Payment.Sbp("+7900")));
        assertTrue(ex.getMessage().contains("не может быть отрицательной"));
    }
}
