package org.hse.shop.application;

import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Отмена заказа: товары возвращаются на склад, за оплаченный заказ возвращаются деньги */
@Service
public class OrderCancellationService {

    private final OrderHistoryService history;
    private final OrderRepository orders;
    private final StockService stock;

    public OrderCancellationService(OrderHistoryService history, OrderRepository orders, StockService stock) {
        this.history = history;
        this.orders = orders;
        this.stock = stock;
    }

    @Transactional
    public Cancellation cancel(long orderId) {
        Order cancelled = history.get(orderId).cancel();
        for (OrderItem item : cancelled.items()) {
            stock.release(item.productId(), item.quantity());
        }
        BigDecimal refund = cancelled.payment() == null ? BigDecimal.ZERO : cancelled.total();
        return new Cancellation(orders.save(cancelled), refund);
    }
}
