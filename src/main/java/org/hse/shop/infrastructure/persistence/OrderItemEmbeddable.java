package org.hse.shop.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.hse.shop.domain.OrderItem;

import java.math.BigDecimal;

/** Позиция заказа в таблице order_items */
@Embeddable
class OrderItemEmbeddable {

    @Column(nullable = false)
    private long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    protected OrderItemEmbeddable() {
    }

    static OrderItemEmbeddable from(OrderItem item) {
        OrderItemEmbeddable embeddable = new OrderItemEmbeddable();
        embeddable.productId = item.productId();
        embeddable.productName = item.productName();
        embeddable.price = item.price();
        embeddable.quantity = item.quantity();
        return embeddable;
    }

    OrderItem toDomain() {
        return new OrderItem(productId, productName, price, quantity);
    }
}
