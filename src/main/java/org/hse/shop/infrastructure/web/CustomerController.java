package org.hse.shop.infrastructure.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hse.shop.application.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Покупатели */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    record NewCustomer(@NotBlank String name, @NotBlank String phone) {
    }

    private final CustomerService customers;

    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse register(@Valid @RequestBody NewCustomer request) {
        return CustomerResponse.from(customers.register(request.name(), request.phone()));
    }

    @GetMapping("/{id}")
    public CustomerResponse get(@PathVariable long id) {
        return CustomerResponse.from(customers.get(id));
    }
}
