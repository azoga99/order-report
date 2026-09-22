package org.hse.shop.application;

import org.hse.shop.domain.BusinessRuleException;
import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.NotFoundException;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderRepository;
import org.hse.shop.domain.OrderStatus;
import org.hse.shop.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Тесты оформления заказа. */
@ExtendWith(MockitoExtension.class)
class OrderPlacementServiceTest {

    private static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-01T09:00:00Z"), MOSCOW);

    @Mock
    private CustomerService customers;

    @Mock
    private StockService stock;

    @Mock
    private DeliveryCostService deliveryCost;

    @Mock
    private OrderRepository orders;

    private OrderPlacementService service;

    @BeforeEach
    void setUp() {
        service = new OrderPlacementService(customers, stock, deliveryCost, orders, CLOCK);
    }

    @Test
    @DisplayName("Оформляет заказ: списывает товары, берёт цены из каталога и считает доставку")
    void placesOrder() {
        when(stock.reserve(1L, 2)).thenReturn(new Product(1L, "Чайник", new BigDecimal("2490.00"), 13));
        when(stock.reserve(2L, 1)).thenReturn(new Product(2L, "Кружка", new BigDecimal("350.00"), 39));
        when(deliveryCost.costFor(DeliveryMethod.COURIER, new BigDecimal("5330.00"))).thenReturn(BigDecimal.ZERO);
        when(orders.save(any())).thenAnswer(call -> call.getArgument(0));

        Order order = service.place(10L, List.of(new OrderLine(1L, 2), new OrderLine(2L, 1)), DeliveryMethod.COURIER);

        assertThat(order.status()).isEqualTo(OrderStatus.NEW);
        assertThat(order.customerId()).isEqualTo(10L);
        assertThat(order.items()).containsExactly(
                new OrderItem(1L, "Чайник", new BigDecimal("2490.00"), 2),
                new OrderItem(2L, "Кружка", new BigDecimal("350.00"), 1));
        assertThat(order.total()).isEqualByComparingTo("5330.00");
        assertThat(order.createdAt()).isEqualTo(LocalDateTime.of(2026, 9, 1, 12, 0));
        verify(customers).get(10L);
        verify(orders).save(order);
    }

    @Test
    @DisplayName("Объединяет строки с одним и тем же товаром")
    void mergesSameProduct() {
        when(stock.reserve(1L, 3)).thenReturn(new Product(1L, "Чайник", new BigDecimal("2490.00"), 12));
        when(deliveryCost.costFor(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orders.save(any())).thenAnswer(call -> call.getArgument(0));

        Order order = service.place(10L, List.of(new OrderLine(1L, 1), new OrderLine(1L, 2)), DeliveryMethod.PICKUP);

        assertThat(order.items()).singleElement().extracting(OrderItem::quantity).isEqualTo(3);
    }

    @Test
    @DisplayName("Добавляет стоимость доставки к сумме заказа")
    void addsDeliveryCost() {
        when(stock.reserve(2L, 1)).thenReturn(new Product(2L, "Кружка", new BigDecimal("350.00"), 39));
        when(deliveryCost.costFor(DeliveryMethod.POST, new BigDecimal("350.00"))).thenReturn(new BigDecimal("290.00"));
        when(orders.save(any())).thenAnswer(call -> call.getArgument(0));

        Order order = service.place(10L, List.of(new OrderLine(2L, 1)), DeliveryMethod.POST);

        assertThat(order.deliveryCost()).isEqualByComparingTo("290.00");
        assertThat(order.total()).isEqualByComparingTo("640.00");
    }

    @Test
    @DisplayName("Не оформляет заказ для неизвестного покупателя")
    void unknownCustomer() {
        when(customers.get(99L)).thenThrow(NotFoundException.customer(99L));

        assertThatThrownBy(() -> service.place(99L, List.of(new OrderLine(1L, 1)), DeliveryMethod.PICKUP))
                .isInstanceOf(NotFoundException.class);
        verify(stock, never()).reserve(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Не оформляет пустой заказ")
    void emptyOrder() {
        assertThatThrownBy(() -> service.place(10L, List.of(), DeliveryMethod.PICKUP))
                .isInstanceOf(IllegalArgumentException.class);
        verify(stock, never()).reserve(anyLong(), anyInt());
    }

    @Test
    @DisplayName("Не сохраняет заказ, если товара не хватает")
    void notEnoughStock() {
        when(stock.reserve(1L, 100)).thenThrow(new BusinessRuleException("Недостаточно товара"));

        assertThatThrownBy(() -> service.place(10L, List.of(new OrderLine(1L, 100)), DeliveryMethod.PICKUP))
                .isInstanceOf(BusinessRuleException.class);
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("Строка корзины с нулевым количеством недопустима (граничный случай)")
    void zeroQuantityLine() {
        assertThatThrownBy(() -> new OrderLine(1L, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
