package org.hse.shop.infrastructure.web;

import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(long id, long customerId, OrderStatus status, List<Item> items,
                            BigDecimal itemsTotal, DeliveryMethod delivery, BigDecimal deliveryCost,
                            BigDecimal total, PaymentDto payment, BigDecimal commission, LocalDateTime createdAt) {

    public record Item(long productId, String name, BigDecimal price, int quantity, BigDecimal amount) {

        static Item from(OrderItem item) {
            return new Item(item.productId(), item.productName(), item.price(), item.quantity(), item.amount());
        }
    }

    static OrderResponse from(Order order) {
        return new OrderResponse(order.id(), order.customerId(), order.status(),
                order.items().stream().map(Item::from).toList(), order.itemsTotal(),
                order.delivery(), order.deliveryCost(), order.total(),
                PaymentDto.from(order.payment()), order.commission(), order.createdAt());
    }
}
