package org.hse.shop.application;

import org.hse.shop.application.SalesReport.ProductSales;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderRepository;
import org.hse.shop.domain.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Отчёт по продажам: выручка, комиссии и самые продаваемые товары за период */
@Service
public class SalesReportService {

    private static final int TOP_SIZE = 5;

    private final OrderRepository orders;

    public SalesReportService(OrderRepository orders) {
        this.orders = orders;
    }

    /** Отчёт по оплаченным заказам, оформленным с from по to включительно */
    public SalesReport build(LocalDate from, LocalDate to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("Конец периода %s раньше начала %s".formatted(to, from));
        }
        List<Order> paid = orders.findCreatedBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay()).stream()
                .filter(order -> order.status() == OrderStatus.PAID)
                .toList();

        BigDecimal revenue = sum(paid, Order::total);
        BigDecimal commission = sum(paid, Order::commission);
        BigDecimal averageCheck = paid.isEmpty()
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(paid.size()), 2, RoundingMode.HALF_UP);

        return new SalesReport(from, to, paid.size(), revenue, commission, revenue.subtract(commission),
                averageCheck, topProducts(paid));
    }

    /** Товары по убыванию выручки; при равной выручке сохраняется порядок первой продажи */
    private static List<ProductSales> topProducts(List<Order> paid) {
        Map<Long, ProductSales> byProduct = new LinkedHashMap<>();
        for (Order order : paid) {
            for (OrderItem item : order.items()) {
                ProductSales sales = new ProductSales(item.productId(), item.productName(), item.quantity(),
                        item.amount());
                byProduct.merge(item.productId(), sales, ProductSales::plus);
            }
        }
        return byProduct.values().stream()
                .sorted(Comparator.comparing(ProductSales::amount).reversed())
                .limit(TOP_SIZE)
                .toList();
    }

    private static BigDecimal sum(List<Order> orders, Function<Order, BigDecimal> field) {
        return orders.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
