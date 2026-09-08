package org.hse.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты контекста приложения. */
class ApplicationContextTest {

    private final ApplicationContext context = ApplicationContext.getContext();

    @Test
    @DisplayName("Возвращает политику комиссий")
    void providesCommissionPolicy() {
        CommissionPolicy policy = context.getInstance("commissionPolicy", CommissionPolicy.class).orElseThrow();

        assertInstanceOf(RateCommissionPolicy.class, policy);
    }

    @Test
    @DisplayName("Возвращает форматтер")
    void providesFormatter() {
        assertTrue(context.getInstance("orderFormatter", OrderFormatter.class).isPresent());
    }

    @Test
    @DisplayName("Сервис отчёта собран с политикой комиссий из контекста")
    void reportServiceUsesContextPolicy() {
        OrderReportService service = context.getInstance("orderReportService", OrderReportService.class).orElseThrow();
        List<Order> orders = List.of(
                new Order(1, "A", new BigDecimal("1000.00"), new Payment.Card("**** 4242", false)), // 15.00
                new Order(2, "B", new BigDecimal("1000.00"), new Payment.Sbp("+7900")));            // 4.00

        assertEquals(0, service.totalCommission(orders).compareTo(new BigDecimal("19.000")));
    }

    @Test
    @DisplayName("Повторное обращение возвращает тот же объект")
    void returnsSameInstance() {
        OrderFormatter first = context.getInstance("orderFormatter", OrderFormatter.class).orElseThrow();
        OrderFormatter second = context.getInstance("orderFormatter", OrderFormatter.class).orElseThrow();

        assertSame(first, second);
    }

    @Test
    @DisplayName("getContext возвращает один и тот же контекст")
    void contextIsSingleton() {
        assertSame(ApplicationContext.getContext(), ApplicationContext.getContext());
    }

    @Test
    @DisplayName("Неизвестное имя даёт пустой Optional")
    void unknownNameGivesEmpty() {
        assertTrue(context.getInstance("unknownService", Object.class).isEmpty());
    }

    @Test
    @DisplayName("Несовпадение типа даёт пустой Optional")
    void typeMismatchGivesEmpty() {
        Optional<OrderFormatter> wrongType = context.getInstance("commissionPolicy", OrderFormatter.class);

        assertTrue(wrongType.isEmpty());
    }
}
