package org.hse.examples;

import java.math.BigDecimal;
import java.util.List;

/**
 * Демонстрационный запуск отчёта по заказам.
 */
public class App {

    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order(1, "Алексей Гусев", new BigDecimal("11191.00"), new Payment.Card("**** 4242", false)),
                new Order(2, "Мария Ковалёва", new BigDecimal("5551.50"), new Payment.Sbp("+79161234567")),
                new Order(3, "Игорь Лебедев", new BigDecimal("5990.00"), new Payment.Cash(new BigDecimal("6000.00"))),
                new Order(4, "Ольга Иванова", new BigDecimal("14080.00"), new Payment.Card("**** 8801", true))
        );

        CommissionPolicy commissionPolicy = new RateCommissionPolicy();
        OrderReportService reportService = new OrderReportService(commissionPolicy);
        OrderFormatter formatter = new OrderFormatter();

        reportService.sortedByAmount(orders).stream()
                .map(order -> formatter.format(order, commissionPolicy.commissionFor(order)))
                .forEach(System.out::println);

        System.out.println("""

                Итого: заказов — %d, комиссия платёжных систем — %s"""
                .formatted(orders.size(), formatter.money(reportService.totalCommission(orders))));
    }
}
