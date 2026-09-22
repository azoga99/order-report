package org.hse.shop.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Тесты расчёта комиссии по фиксированным ставкам. */
class RateCommissionPolicyTest {

    private final RateCommissionPolicy policy = new RateCommissionPolicy();

    private static void assertMoneyEquals(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)),
                () -> "ожидалось " + expected + ", получено " + actual);
    }

    @Test
    @DisplayName("Карта российского банка — 1.5%")
    void domesticCard() {
        Order order = new Order(1, "A", new BigDecimal("1000.00"), new Payment.Card("**** 1111", false));
        assertMoneyEquals("15.000", policy.commissionFor(order));
    }

    @Test
    @DisplayName("Карта зарубежного банка — 3.5%")
    void foreignCard() {
        Order order = new Order(2, "B", new BigDecimal("1000.00"), new Payment.Card("**** 2222", true));
        assertMoneyEquals("35.000", policy.commissionFor(order));
    }

    @Test
    @DisplayName("СБП — 0.4%")
    void sbp() {
        Order order = new Order(3, "C", new BigDecimal("1000.00"), new Payment.Sbp("+7900"));
        assertMoneyEquals("4.000", policy.commissionFor(order));
    }

    @Test
    @DisplayName("Наличные — без комиссии")
    void cash() {
        Order order = new Order(4, "D", new BigDecimal("1000.00"), new Payment.Cash(new BigDecimal("1000.00")));
        assertMoneyEquals("0", policy.commissionFor(order));
    }

    @Test
    @DisplayName("Нулевая сумма даёт нулевую комиссию (граничный случай)")
    void zeroAmount() {
        Order order = new Order(5, "E", BigDecimal.ZERO, new Payment.Card("**** 3333", true));
        assertMoneyEquals("0", policy.commissionFor(order));
    }
}
