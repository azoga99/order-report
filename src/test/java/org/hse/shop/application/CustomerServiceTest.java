package org.hse.shop.application;

import org.hse.shop.domain.BusinessRuleException;
import org.hse.shop.domain.Customer;
import org.hse.shop.domain.CustomerRepository;
import org.hse.shop.domain.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Тесты регистрации покупателей. */
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customers;

    @InjectMocks
    private CustomerService service;

    @Test
    @DisplayName("Регистрирует покупателя с новым телефоном")
    void registers() {
        when(customers.existsByPhone("+79161234567")).thenReturn(false);
        when(customers.save(any())).thenAnswer(call -> {
            Customer customer = call.getArgument(0);
            return new Customer(5L, customer.name(), customer.phone());
        });

        Customer registered = service.register("Мария", "+79161234567");

        assertThat(registered.id()).isEqualTo(5L);
        assertThat(registered.name()).isEqualTo("Мария");
    }

    @Test
    @DisplayName("Не регистрирует второй раз тот же телефон")
    void rejectsDuplicatePhone() {
        when(customers.existsByPhone("+79161234567")).thenReturn(true);

        assertThatThrownBy(() -> service.register("Мария", "+79161234567"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("уже зарегистрирован");
        verify(customers, never()).save(any());
    }

    @Test
    @DisplayName("Некорректный телефон отклоняется до обращения к хранилищу")
    void validatesBeforeLookup() {
        assertThatThrownBy(() -> service.register("Мария", "12345"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(customers);
    }

    @Test
    @DisplayName("Находит покупателя по id")
    void getsCustomer() {
        Customer customer = new Customer(1L, "Алексей", "+79031112233");
        when(customers.findById(1L)).thenReturn(Optional.of(customer));

        assertThat(service.get(1L)).isEqualTo(customer);
    }

    @Test
    @DisplayName("Сообщает, что покупатель не найден")
    void failsOnMissingCustomer() {
        when(customers.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(3L)).isInstanceOf(NotFoundException.class);
    }
}
