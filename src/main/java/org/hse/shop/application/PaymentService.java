package org.hse.shop.application;

import org.hse.shop.domain.CommissionPolicy;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderRepository;
import org.hse.shop.domain.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Оплата заказа с расчётом комиссии платёжной системы */
@Service
public class PaymentService {

    private final OrderHistoryService history;
    private final OrderRepository orders;
    private final CommissionPolicy commissionPolicy;

    public PaymentService(OrderHistoryService history, OrderRepository orders, CommissionPolicy commissionPolicy) {
        this.history = history;
        this.orders = orders;
        this.commissionPolicy = commissionPolicy;
    }

    /** Оплачивает новый заказ. Комиссия считается от полной суммы вместе с доставкой */
    public Order pay(long orderId, Payment payment) {
        Order order = history.get(orderId);
        BigDecimal commission = commissionPolicy.commissionFor(order.total(), payment);
        return orders.save(order.pay(payment, commission));
    }
}
