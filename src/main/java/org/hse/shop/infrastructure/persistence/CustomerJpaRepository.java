package org.hse.shop.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface CustomerJpaRepository extends JpaRepository<CustomerEntity, Long> {

    boolean existsByPhone(String phone);
}
