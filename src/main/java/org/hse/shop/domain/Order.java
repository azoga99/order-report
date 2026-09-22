package org.hse.shop.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Заказ покупателя.
 * Оплата и комиссия заполняются только после оплаты, до этого payment равен null.
 */
public record Order(Long id, long customerId, List<OrderItem> items, DeliveryMethod delivery,
                    BigDecimal deliveryCost, OrderStatus status, Payment payment, BigDecimal commission,
                    LocalDateTime createdAt) {

    public Order {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("В заказе должен быть хотя бы один товар");
        }
        items = List.copyOf(items);
        Objects.requireNonNull(delivery, "Не указан способ доставки");
        Objects.requireNonNull(status, "Не указан статус заказа");
        if (deliveryCost == null || deliveryCost.signum() < 0) {
            throw new IllegalArgumentException("Стоимость доставки не может быть отрицательной: " + deliveryCost);
        }
    }

    /** Новый неоплаченный заказ; id назначит хранилище */
    public static Order create(long customerId, List<OrderItem> items, DeliveryMethod delivery,
                               BigDecimal deliveryCost, LocalDateTime createdAt) {
        return new Order(null, customerId, items, delivery, deliveryCost, OrderStatus.NEW, null,
                BigDecimal.ZERO, createdAt);
    }

    /** Стоимость товаров без доставки */
    public BigDecimal itemsTotal() {
        return items.stream()
                .map(OrderItem::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Сумма к оплате: товары и доставка */
    public BigDecimal total() {
        return itemsTotal().add(deliveryCost);
    }

    /** Заказ после оплаты */
    public Order pay(Payment payment, BigDecimal commission) {
        Objects.requireNonNull(payment, "Не указан способ оплаты");
        if (status != OrderStatus.NEW) {
            throw new BusinessRuleException("Оплатить можно только новый заказ, этот заказ " + status.title());
        }
        if (payment instanceof Payment.Cash cash && cash.received().compareTo(total()) < 0) {
            throw new BusinessRuleException("Получено наличными %s, а к оплате %s"
                    .formatted(cash.received(), total()));
        }
        return new Order(id, customerId, items, delivery, deliveryCost, OrderStatus.PAID, payment, commission,
                createdAt);
    }

    /** Заказ после отмены. Оплата остаётся в заказе, чтобы было видно, что деньги нужно вернуть */
    public Order cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Заказ уже отменён");
        }
        return new Order(id, customerId, items, delivery, deliveryCost, OrderStatus.CANCELLED, payment,
                commission, createdAt);
    }
}
