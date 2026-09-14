package org.hse.examples;

import org.hse.examples.application.OrderReportService;
import org.hse.examples.domain.CommissionPolicy;
import org.hse.examples.domain.Order;
import org.hse.examples.domain.Payment;
import org.hse.examples.domain.RateCommissionPolicy;
import org.hse.examples.infrastructure.OrderFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Тесты контекста Spring: какие объекты в нём есть и как они связаны. */
@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Возвращает политику комиссий")
    void providesCommissionPolicy() {
        CommissionPolicy policy = context.getBean("commissionPolicy", CommissionPolicy.class);

        assertInstanceOf(RateCommissionPolicy.class, policy);
    }

    @Test
    @DisplayName("Возвращает форматтер")
    void providesFormatter() {
        assertNotNull(context.getBean("orderFormatter", OrderFormatter.class));
    }

    @Test
    @DisplayName("Сервис отчёта собран с политикой комиссий из контекста")
    void reportServiceUsesContextPolicy() {
        OrderReportService service = context.getBean("orderReportService", OrderReportService.class);
        List<Order> orders = List.of(
                new Order(1, "A", new BigDecimal("1000.00"), new Payment.Card("**** 4242", false)), // 15.00
                new Order(2, "B", new BigDecimal("1000.00"), new Payment.Sbp("+7900")));            // 4.00

        assertEquals(0, service.totalCommission(orders).compareTo(new BigDecimal("19.000")));
    }

    @Test
    @DisplayName("Повторное обращение возвращает тот же объект")
    void returnsSameInstance() {
        OrderFormatter first = context.getBean("orderFormatter", OrderFormatter.class);
        OrderFormatter second = context.getBean("orderFormatter", OrderFormatter.class);

        assertSame(first, second);
    }

    @Test
    @DisplayName("По имени и по типу возвращается один и тот же объект")
    void sameInstanceByNameAndType() {
        assertSame(context.getBean("commissionPolicy"), context.getBean(CommissionPolicy.class));
    }

    @Test
    @DisplayName("Неизвестное имя приводит к ошибке")
    void unknownNameFails() {
        assertThrows(NoSuchBeanDefinitionException.class, () -> context.getBean("unknownService"));
    }

    @Test
    @DisplayName("Несовпадение типа приводит к ошибке")
    void typeMismatchFails() {
        assertThrows(BeanNotOfRequiredTypeException.class,
                () -> context.getBean("commissionPolicy", OrderFormatter.class));
    }
}
