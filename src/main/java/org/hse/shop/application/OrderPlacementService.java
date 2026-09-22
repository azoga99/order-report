package org.hse.shop.application;

import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderRepository;
import org.hse.shop.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Оформление заказа: проверка покупателя, списание товаров со склада и расчёт доставки */
@Service
public class OrderPlacementService {

    private final CustomerService customers;
    private final StockService stock;
    private final DeliveryCostService deliveryCost;
    private final OrderRepository orders;
    private final Clock clock;

    public OrderPlacementService(CustomerService customers, StockService stock, DeliveryCostService deliveryCost,
                                 OrderRepository orders, Clock clock) {
        this.customers = customers;
        this.stock = stock;
        this.deliveryCost = deliveryCost;
        this.orders = orders;
        this.clock = clock;
    }

    /**
     * Оформляет заказ. Строки с одним и тем же товаром объединяются.
     * Если какого-то товара не хватает, заказ не создаётся и списания по другим товарам откатываются.
     */
    @Transactional
    public Order place(long customerId, List<OrderLine> lines, DeliveryMethod delivery) {
        customers.get(customerId);
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("В заказе должен быть хотя бы один товар");
        }

        List<OrderItem> items = new ArrayList<>();
        mergeSameProducts(lines).forEach((productId, quantity) -> {
            Product product = stock.reserve(productId, quantity);
            items.add(new OrderItem(product.id(), product.name(), product.price(), quantity));
        });

        BigDecimal itemsTotal = items.stream().map(OrderItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = Order.create(customerId, items, delivery, deliveryCost.costFor(delivery, itemsTotal),
                LocalDateTime.now(clock));
        return orders.save(order);
    }

    private static Map<Long, Integer> mergeSameProducts(List<OrderLine> lines) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderLine line : lines) {
            quantities.merge(line.productId(), line.quantity(), Integer::sum);
        }
        return quantities;
    }
}
