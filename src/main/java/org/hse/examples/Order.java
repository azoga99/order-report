package org.hse.examples;

import java.math.BigDecimal;

/** Заказ покупателя */
public record Order(int id, String customer, BigDecimal amount, Payment payment) {

    public Order {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Сумма заказа не может быть отрицательной: " + amount);
        }
    }
}
