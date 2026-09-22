package org.hse.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByCustomerId(long customerId);

    @Query("select o from OrderEntity o where o.createdAt >= :from and o.createdAt < :to")
    List<OrderEntity> findCreatedBetween(LocalDateTime from, LocalDateTime to);
}
