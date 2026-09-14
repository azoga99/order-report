package org.hse.examples.infrastructure;

import org.hse.examples.application.OrderReportService;
import org.hse.examples.domain.CommissionPolicy;
import org.hse.examples.domain.Order;
import org.hse.examples.domain.Payment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Вывод отчёта по демонстрационным заказам при старте приложения */
@Component
public class OrderReportRunner implements CommandLineRunner {

    private final CommissionPolicy commissionPolicy;
    private final OrderReportService reportService;
    private final OrderFormatter formatter;

    public OrderReportRunner(CommissionPolicy commissionPolicy, OrderReportService reportService,
                             OrderFormatter formatter) {
        this.commissionPolicy = commissionPolicy;
        this.reportService = reportService;
        this.formatter = formatter;
    }

    @Override
    public void run(String... args) {
        List<Order> orders = List.of(
                new Order(1, "Алексей Гусев", new BigDecimal("11191.00"), new Payment.Card("**** 4242", false)),
                new Order(2, "Мария Ковалёва", new BigDecimal("5551.50"), new Payment.Sbp("+79161234567")),
                new Order(3, "Игорь Лебедев", new BigDecimal("5990.00"), new Payment.Cash(new BigDecimal("6000.00"))),
                new Order(4, "Ольга Иванова", new BigDecimal("14080.00"), new Payment.Card("**** 8801", true))
        );

        reportService.sortedByAmount(orders).stream()
                .map(order -> formatter.format(order, commissionPolicy.commissionFor(order)))
                .forEach(System.out::println);

        System.out.println("""

                Итого: заказов — %d, комиссия платёжных систем — %s"""
                .formatted(orders.size(), formatter.money(reportService.totalCommission(orders))));
    }
}
