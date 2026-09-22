package org.hse.shop.application;

import org.hse.shop.domain.BusinessRuleException;
import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderRepository;
import org.hse.shop.domain.OrderStatus;
import org.hse.shop.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Тесты отмены заказа. */
@ExtendWith(MockitoExtension.class)
class OrderCancellationServiceTest {

    @Mock
    private OrderHistoryService history;

    @Mock
    private OrderRepository orders;

    @Mock
    private StockService stock;

    @InjectMocks
    private OrderCancellationService service;

    /** Два чайника и кружка с доставкой почтой: 2 × 2490 + 350 + 290 = 5620 руб. */
    private final Order newOrder = Order.create(1L, List.of(
                    new OrderItem(1L, "Чайник", new BigDecimal("2490.00"), 2),
                    new OrderItem(2L, "Кружка", new BigDecimal("350.00"), 1)),
            DeliveryMethod.POST, new BigDecimal("290.00"), LocalDateTime.of(2026, 9, 1, 12, 0));

    @Test
    @DisplayName("Отменяет новый заказ, возвращает товары на склад, денег не возвращает")
    void cancelsNewOrder() {
        when(history.get(5L)).thenReturn(newOrder);
        when(orders.save(any())).thenAnswer(call -> call.getArgument(0));

        Cancellation result = service.cancel(5L);

        assertThat(result.order().status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.refund()).isZero();
        verify(stock).release(1L, 2);
        verify(stock).release(2L, 1);
    }

    @Test
    @DisplayName("За оплаченный заказ возвращается вся сумма вместе с доставкой")
    void refundsPaidOrder() {
        when(history.get(5L)).thenReturn(newOrder.pay(new Payment.Sbp("+79161234567"), new BigDecimal("22.48")));
        when(orders.save(any())).thenAnswer(call -> call.getArgument(0));

        Cancellation result = service.cancel(5L);

        assertThat(result.refund()).isEqualByComparingTo("5620.00");
    }

    @Test
    @DisplayName("Повторная отмена отклоняется, склад не трогается")
    void rejectsSecondCancellation() {
        when(history.get(5L)).thenReturn(newOrder.cancel());

        assertThatThrownBy(() -> service.cancel(5L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("уже отменён");
        verifyNoInteractions(stock);
        verify(orders, never()).save(any());
    }
}
