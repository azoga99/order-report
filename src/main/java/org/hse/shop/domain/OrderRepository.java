package org.hse.shop.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Хранилище заказов */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(long id);

    List<Order> findByCustomerId(long customerId);

    /** Заказы, оформленные в промежутке [from, to) */
    List<Order> findCreatedBetween(LocalDateTime from, LocalDateTime to);
}
