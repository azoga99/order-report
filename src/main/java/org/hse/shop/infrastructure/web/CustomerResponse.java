package org.hse.shop.infrastructure.web;

import org.hse.shop.domain.Customer;

public record CustomerResponse(long id, String name, String phone) {

    static CustomerResponse from(Customer customer) {
        return new CustomerResponse(customer.id(), customer.name(), customer.phone());
    }
}
