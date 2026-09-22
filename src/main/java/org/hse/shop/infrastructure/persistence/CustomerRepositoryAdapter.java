package org.hse.shop.infrastructure.persistence;

import org.hse.shop.domain.Customer;
import org.hse.shop.domain.CustomerRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Хранилище покупателей поверх Spring Data JPA */
@Repository
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpa;

    CustomerRepositoryAdapter(CustomerJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Customer save(Customer customer) {
        return jpa.save(CustomerEntity.from(customer)).toDomain();
    }

    @Override
    public Optional<Customer> findById(long id) {
        return jpa.findById(id).map(CustomerEntity::toDomain);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return jpa.existsByPhone(phone);
    }
}
