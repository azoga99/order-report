package org.hse.shop.infrastructure.persistence;

import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Хранилище заказов поверх Spring Data JPA */
@Repository
public class OrderRepositoryAdapter implements OrderRepository {

    private final OrderJpaRepository jpa;

    OrderRepositoryAdapter(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order save(Order order) {
        return jpa.save(OrderEntity.from(order)).toDomain();
    }

    @Override
    public Optional<Order> findById(long id) {
        return jpa.findById(id).map(OrderEntity::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(long customerId) {
        return jpa.findByCustomerId(customerId).stream().map(OrderEntity::toDomain).toList();
    }

    @Override
    public List<Order> findCreatedBetween(LocalDateTime from, LocalDateTime to) {
        return jpa.findCreatedBetween(from, to).stream().map(OrderEntity::toDomain).toList();
    }
}
