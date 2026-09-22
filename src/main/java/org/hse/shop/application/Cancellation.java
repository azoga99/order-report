package org.hse.shop.application;

import org.hse.shop.domain.Order;

import java.math.BigDecimal;

/** Итог отмены: отменённый заказ и сумма, которую нужно вернуть покупателю */
public record Cancellation(Order order, BigDecimal refund) {
}
