package org.hse.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Интеграционный сценарий (политика комиссий + сервис + форматтер)
 */
class AppTest {

    @Test
    @DisplayName("Считает итоговую комиссию по смешанному списку заказов")
    void endToEndTotalCommission() {
        List<Order> orders = List.of(
                new Order(1, "Алексей", new BigDecimal("1000.00"), new Payment.Card("**** 4242", false)), // 15.00
                new Order(2, "Мария", new BigDecimal("1000.00"), new Payment.Sbp("+7900")),                // 4.00
                new Order(3, "Игорь", new BigDecimal("1000.00"), new Payment.Cash(new BigDecimal("1000"))),// 0
                new Order(4, "Ольга", new BigDecimal("1000.00"), new Payment.Card("**** 8801", true))      // 35.00
        );

        CommissionPolicy policy = new RateCommissionPolicy();
        OrderReportService service = new OrderReportService(policy);

        assertEquals(0, service.totalCommission(orders).compareTo(new BigDecimal("54.000")));
    }

    @Test
    @DisplayName("main выполняется без ошибок")
    void mainRuns() {
        assertDoesNotThrow(() -> App.main(new String[]{}));
    }
}
