package org.hse.examples.application;

import org.hse.examples.domain.CommissionPolicy;
import org.hse.examples.domain.Order;
import org.hse.examples.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты сортировки и агрегации по списку заказов. */
class OrderReportServiceTest {

    private final Order small = new Order(1, "A", new BigDecimal("100.00"), new Payment.Sbp("+7900"));
    private final Order medium = new Order(2, "B", new BigDecimal("500.00"), new Payment.Sbp("+7901"));
    private final Order large = new Order(3, "C", new BigDecimal("900.00"), new Payment.Sbp("+7902"));

    /** Заглушка политики: возвращает заранее заданную комиссию по id заказа. */
    private static CommissionPolicy stubPolicy(Map<Integer, String> byId) {
        return order -> new BigDecimal(byId.getOrDefault(order.id(), "0"));
    }

    @Test
    @DisplayName("Сортирует заказы по убыванию суммы")
    void sortsByAmountDescending() {
        OrderReportService service = new OrderReportService(stubPolicy(Map.of()));

        List<Order> sorted = service.sortedByAmount(List.of(small, large, medium));

        assertEquals(List.of(large, medium, small), sorted);
    }

    @Test
    @DisplayName("Не изменяет исходный список при сортировке")
    void doesNotMutateInput() {
        OrderReportService service = new OrderReportService(stubPolicy(Map.of()));
        List<Order> input = List.of(small, large, medium);

        service.sortedByAmount(input);

        assertEquals(List.of(small, large, medium), input);
    }

    @Test
    @DisplayName("Пустой список сортируется в пустой (граничный случай)")
    void sortsEmptyList() {
        OrderReportService service = new OrderReportService(stubPolicy(Map.of()));
        assertTrue(service.sortedByAmount(List.of()).isEmpty());
    }

    @Test
    @DisplayName("Сохраняет исходный порядок заказов с одинаковой суммой")
    void keepsOrderOfEqualAmounts() {
        OrderReportService service = new OrderReportService(stubPolicy(Map.of()));
        Order duplicate = new Order(4, "D", new BigDecimal("500.00"), new Payment.Sbp("+7903"));

        List<Order> sorted = service.sortedByAmount(List.of(duplicate, medium, large));

        assertEquals(List.of(large, duplicate, medium), sorted);
    }

    @Test
    @DisplayName("Суммирует комиссии всех заказов, используя переданную политику")
    void sumsCommissions() {
        OrderReportService service = new OrderReportService(
                stubPolicy(Map.of(small.id(), "1.00", large.id(), "2.50")));

        BigDecimal total = service.totalCommission(List.of(small, large));

        assertEquals(0, total.compareTo(new BigDecimal("3.50")));
    }

    @Test
    @DisplayName("Итоговая комиссия пустого списка равна нулю (граничный случай)")
    void totalOfEmptyListIsZero() {
        OrderReportService service = new OrderReportService(stubPolicy(Map.of()));
        assertEquals(0, service.totalCommission(List.of()).compareTo(BigDecimal.ZERO));
    }
}
