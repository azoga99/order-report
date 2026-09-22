package org.hse.shop.application;

import org.hse.shop.domain.BusinessRuleException;
import org.hse.shop.domain.CommissionPolicy;
import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.NotFoundException;
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
import static org.mockito.Mockito.when;

/** Тесты оплаты заказа. */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderHistoryService history;

    @Mock
    private OrderRepository orders;

    @Mock
    private CommissionPolicy commissionPolicy;

    @InjectMocks
    private PaymentService service;

    /** Заказ на 2490 руб. и 390 руб. доставки, итого 2880 руб. */
    private final Order newOrder = Order.create(1L,
            List.of(new OrderItem(1L, "Чайник", new BigDecimal("2490.00"), 1)),
            DeliveryMethod.COURIER, new BigDecimal("390.00"), LocalDateTime.of(2026, 9, 1, 12, 0));

    @Test
    @DisplayName("Оплачивает заказ и сохраняет комиссию, посчитанную от суммы с доставкой")
    void paysOrder() {
        Payment card = new Payment.Card("**** 4242", false);
        when(history.get(5L)).thenReturn(newOrder);
        when(commissionPolicy.commissionFor(new BigDecimal("2880.00"), card)).thenReturn(new BigDecimal("43.20"));
        when(orders.save(any())).thenAnswer(call -> call.getArgument(0));

        Order paid = service.pay(5L, card);

        assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
        assertThat(paid.payment()).isEqualTo(card);
        assertThat(paid.commission()).isEqualByComparingTo("43.20");
        verify(orders).save(paid);
    }

    @Test
    @DisplayName("Повторная оплата отклоняется и ничего не сохраняет")
    void rejectsSecondPayment() {
        Payment sbp = new Payment.Sbp("+79161234567");
        when(history.get(5L)).thenReturn(newOrder.pay(sbp, BigDecimal.ONE));
        when(commissionPolicy.commissionFor(any(), any())).thenReturn(BigDecimal.ONE);

        assertThatThrownBy(() -> service.pay(5L, sbp))
                .isInstanceOf(BusinessRuleException.class);
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("Наличных меньше суммы заказа — оплата отклоняется")
    void rejectsNotEnoughCash() {
        when(history.get(5L)).thenReturn(newOrder);
        when(commissionPolicy.commissionFor(any(), any())).thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> service.pay(5L, new Payment.Cash(new BigDecimal("2500.00"))))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("к оплате 2880.00");
        verify(orders, never()).save(any());
    }

    @Test
    @DisplayName("Нельзя оплатить несуществующий заказ")
    void missingOrder() {
        when(history.get(404L)).thenThrow(NotFoundException.order(404L));

        assertThatThrownBy(() -> service.pay(404L, new Payment.Sbp("+79161234567")))
                .isInstanceOf(NotFoundException.class);
    }
}
