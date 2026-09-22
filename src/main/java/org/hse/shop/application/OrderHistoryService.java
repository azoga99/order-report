package org.hse.shop.application;

import org.hse.shop.domain.NotFoundException;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/** Просмотр заказов: заказ по номеру и история заказов покупателя */
@Service
public class OrderHistoryService {

    private final OrderRepository orders;
    private final CustomerService customers;

    public OrderHistoryService(OrderRepository orders, CustomerService customers) {
        this.orders = orders;
        this.customers = customers;
    }

    public Order get(long orderId) {
        return orders.findById(orderId).orElseThrow(() -> NotFoundException.order(orderId));
    }

    /** Заказы покупателя, сначала новые */
    public List<Order> ofCustomer(long customerId) {
        customers.get(customerId);
        return orders.findByCustomerId(customerId).stream()
                .sorted(Comparator.comparing(Order::createdAt).reversed())
                .toList();
    }
}
