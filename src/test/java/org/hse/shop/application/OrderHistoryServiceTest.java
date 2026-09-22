package org.hse.shop.application;

import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.NotFoundException;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderRepository;
import org.hse.shop.domain.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Тесты просмотра заказов. */
@ExtendWith(MockitoExtension.class)
class OrderHistoryServiceTest {

    @Mock
    private OrderRepository orders;

    @Mock
    private CustomerService customers;

    @InjectMocks
    private OrderHistoryService history;

    private static Order order(long id, LocalDateTime createdAt) {
        return new Order(id, 1L, List.of(new OrderItem(1L, "Чайник", new BigDecimal("2490.00"), 1)),
                DeliveryMethod.PICKUP, BigDecimal.ZERO, OrderStatus.NEW, null, BigDecimal.ZERO, createdAt);
    }

    @Test
    @DisplayName("Находит заказ по номеру")
    void getsOrder() {
        Order order = order(3L, LocalDateTime.of(2026, 9, 1, 10, 0));
        when(orders.findById(3L)).thenReturn(Optional.of(order));

        assertThat(history.get(3L)).isEqualTo(order);
    }

    @Test
    @DisplayName("Сообщает, что заказ не найден")
    void failsOnMissingOrder() {
        when(orders.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> history.get(3L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Заказ не найден: 3");
    }

    @Test
    @DisplayName("Показывает заказы покупателя, сначала новые")
    void customerOrdersNewestFirst() {
        Order older = order(1L, LocalDateTime.of(2026, 9, 1, 10, 0));
        Order newer = order(2L, LocalDateTime.of(2026, 9, 5, 10, 0));
        when(orders.findByCustomerId(1L)).thenReturn(List.of(older, newer));

        assertThat(history.ofCustomer(1L)).containsExactly(newer, older);
    }

    @Test
    @DisplayName("У покупателя без заказов — пустая история (граничный случай)")
    void noOrders() {
        when(orders.findByCustomerId(1L)).thenReturn(List.of());

        assertThat(history.ofCustomer(1L)).isEmpty();
    }

    @Test
    @DisplayName("Для неизвестного покупателя — ошибка, а не пустой список")
    void unknownCustomer() {
        when(customers.get(99L)).thenThrow(NotFoundException.customer(99L));

        assertThatThrownBy(() -> history.ofCustomer(99L)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(orders);
    }
}
