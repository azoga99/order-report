package org.hse.examples;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/** Расчёты по списку заказов: сортировка и итоговые суммы */
public class OrderReportService {

    private final CommissionPolicy commissionPolicy;

    public OrderReportService(CommissionPolicy commissionPolicy) {
        this.commissionPolicy = commissionPolicy;
    }

    /** Заказы, отсортированные по убыванию суммы */
    public List<Order> sortedByAmount(List<Order> orders) {
        return orders.stream()
                .sorted(Comparator.comparing(Order::amount).reversed())
                .toList();
    }

    /** Суммарная комиссия по всем заказам */
    public BigDecimal totalCommission(List<Order> orders) {
        return orders.stream()
                .map(commissionPolicy::commissionFor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
