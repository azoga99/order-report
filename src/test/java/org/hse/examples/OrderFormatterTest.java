package org.hse.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты форматирования заказов и сумм. */
class OrderFormatterTest {

    private final OrderFormatter formatter = new OrderFormatter();

    @Test
    @DisplayName("Округляет сумму до копеек по правилу HALF_UP")
    void moneyRoundsHalfUp() {
        assertEquals("10.01 руб.", formatter.money(new BigDecimal("10.005")));
        assertEquals("10.00 руб.", formatter.money(new BigDecimal("10.004")));
    }

    @Test
    @DisplayName("Дополняет целое число до двух знаков")
    void moneyPadsScale() {
        assertEquals("5.00 руб.", formatter.money(new BigDecimal("5")));
    }

    @Test
    @DisplayName("Форматирует заказ с картой российского банка")
    void formatsDomesticCard() {
        Order order = new Order(1, "Иван", new BigDecimal("100.00"), new Payment.Card("**** 4242", false));
        assertEquals("№1 Иван — 100.00 руб., оплата: карта **** 4242 (российский банк), комиссия: 1.50 руб.",
                formatter.format(order, new BigDecimal("1.50")));
    }

    @Test
    @DisplayName("Форматирует заказ с картой зарубежного банка")
    void formatsForeignCard() {
        Order order = new Order(2, "Ольга", new BigDecimal("200.00"), new Payment.Card("**** 8801", true));
        assertTrue(formatter.format(order, new BigDecimal("7.00"))
                .contains("карта **** 8801 (зарубежный банк)"));
    }

    @Test
    @DisplayName("Форматирует заказ по СБП")
    void formatsSbp() {
        Order order = new Order(3, "Мария", new BigDecimal("50.00"), new Payment.Sbp("+79161234567"));
        assertTrue(formatter.format(order, new BigDecimal("0.20"))
                .contains("оплата: СБП +79161234567"));
    }

    @Test
    @DisplayName("Форматирует заказ наличными с суммой полученного")
    void formatsCash() {
        Order order = new Order(4, "Игорь", new BigDecimal("990.00"), new Payment.Cash(new BigDecimal("1000.00")));
        String result = formatter.format(order, BigDecimal.ZERO);
        assertTrue(result.contains("наличные, получено 1000.00 руб."), result);
        assertTrue(result.endsWith("комиссия: 0.00 руб."), result);
    }
}
