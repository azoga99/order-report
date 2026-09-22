package org.hse.shop.domain;

import java.util.Optional;

/** Хранилище покупателей */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(long id);

    boolean existsByPhone(String phone);
}
