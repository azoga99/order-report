package org.hse.shop.application;

import org.hse.shop.domain.BusinessRuleException;
import org.hse.shop.domain.Customer;
import org.hse.shop.domain.CustomerRepository;
import org.hse.shop.domain.NotFoundException;
import org.springframework.stereotype.Service;

/** Регистрация покупателей */
@Service
public class CustomerService {

    private final CustomerRepository customers;

    public CustomerService(CustomerRepository customers) {
        this.customers = customers;
    }

    /** Регистрирует покупателя. Один телефон — один покупатель */
    public Customer register(String name, String phone) {
        Customer customer = Customer.create(name, phone);
        if (customers.existsByPhone(phone)) {
            throw new BusinessRuleException("Покупатель с телефоном " + phone + " уже зарегистрирован");
        }
        return customers.save(customer);
    }

    public Customer get(long id) {
        return customers.findById(id).orElseThrow(() -> NotFoundException.customer(id));
    }
}
