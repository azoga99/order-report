package org.hse.shop.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderStatus;
import org.hse.shop.domain.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Строка таблицы заказов.
 * Способ оплаты разложен по колонкам: какие из них заполнены, зависит от payment_method.
 */
@Entity
@Table(name = "orders")
class OrderEntity {

    enum PaymentMethod { CARD, SBP, CASH }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private long customerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @OrderColumn(name = "position")
    private List<OrderItemEmbeddable> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryMethod delivery;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deliveryCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String cardNumber;

    private Boolean foreignIssuer;

    private String sbpPhone;

    @Column(precision = 12, scale = 2)
    private BigDecimal cashReceived;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal commission;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected OrderEntity() {
    }

    static OrderEntity from(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.id = order.id();
        entity.customerId = order.customerId();
        entity.items = new ArrayList<>(order.items().stream().map(OrderItemEmbeddable::from).toList());
        entity.delivery = order.delivery();
        entity.deliveryCost = order.deliveryCost();
        entity.status = order.status();
        entity.commission = order.commission();
        entity.createdAt = order.createdAt();
        switch (order.payment()) {
            case null -> {
            }
            case Payment.Card(var number, var foreign) -> {
                entity.paymentMethod = PaymentMethod.CARD;
                entity.cardNumber = number;
                entity.foreignIssuer = foreign;
            }
            case Payment.Sbp(var phone) -> {
                entity.paymentMethod = PaymentMethod.SBP;
                entity.sbpPhone = phone;
            }
            case Payment.Cash(var received) -> {
                entity.paymentMethod = PaymentMethod.CASH;
                entity.cashReceived = received;
            }
        }
        return entity;
    }

    Order toDomain() {
        return new Order(id, customerId, items.stream().map(OrderItemEmbeddable::toDomain).toList(),
                delivery, deliveryCost, status, payment(), commission, createdAt);
    }

    private Payment payment() {
        if (paymentMethod == null) {
            return null;
        }
        return switch (paymentMethod) {
            case CARD -> new Payment.Card(cardNumber, foreignIssuer);
            case SBP -> new Payment.Sbp(sbpPhone);
            case CASH -> new Payment.Cash(cashReceived);
        };
    }
}
