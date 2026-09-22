package org.hse.shop.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Отчёт по продажам за период.
 * Выручка включает доставку, чистая выручка — выручка за вычетом комиссий платёжных систем.
 */
public record SalesReport(LocalDate from, LocalDate to, int ordersCount, BigDecimal revenue,
                          BigDecimal commission, BigDecimal netRevenue, BigDecimal averageCheck,
                          List<ProductSales> topProducts) {

    /** Сколько штук товара продано и на какую сумму */
    public record ProductSales(long productId, String name, int quantity, BigDecimal amount) {

        ProductSales plus(ProductSales other) {
            return new ProductSales(productId, name, quantity + other.quantity, amount.add(other.amount));
        }
    }
}
