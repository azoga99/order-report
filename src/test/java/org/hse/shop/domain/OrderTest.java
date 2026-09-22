package org.hse.shop.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Тесты заказа: суммы и переходы между статусами. */
class OrderTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 1, 12, 0);

    private final OrderItem kettle = new OrderItem(1L, "Чайник", new BigDecimal("2490.00"), 2);
    private final OrderItem mug = new OrderItem(2L, "Кружка", new BigDecimal("350.00"), 1);

    private Order newOrder() {
        return Order.create(10L, List.of(kettle, mug), DeliveryMethod.COURIER, new BigDecimal("390.00"), NOW);
    }

    @Test
    @DisplayName("Новый заказ — без оплаты и без комиссии")
    void createsNewOrder() {
        Order order = newOrder();

        assertThat(order.status()).isEqualTo(OrderStatus.NEW);
        assertThat(order.payment()).isNull();
        assertThat(order.commission()).isZero();
        assertThat(order.createdAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("Стоимость позиции — цена, умноженная на количество")
    void itemAmount() {
        assertThat(kettle.amount()).isEqualByComparingTo("4980.00");
    }

    @Test
    @DisplayName("Считает сумму товаров и сумму к оплате с доставкой")
    void calculatesTotals() {
        Order order = newOrder();

        assertThat(order.itemsTotal()).isEqualByComparingTo("5330.00");
        assertThat(order.total()).isEqualByComparingTo("5720.00");
    }

    @Test
    @DisplayName("Отклоняет заказ без товаров")
    void rejectsEmptyOrder() {
        assertThatThrownBy(() -> Order.create(10L, List.of(), DeliveryMethod.PICKUP, BigDecimal.ZERO, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("хотя бы один товар");
    }

    @Test
    @DisplayName("Отклоняет отрицательную стоимость доставки")
    void rejectsNegativeDeliveryCost() {
        assertThatThrownBy(() -> Order.create(10L, List.of(mug), DeliveryMethod.POST, new BigDecimal("-1"), NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Отклоняет позицию с нулевым количеством (граничный случай)")
    void rejectsZeroQuantity() {
        assertThatThrownBy(() -> new OrderItem(1L, "Чайник", BigDecimal.TEN, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Список позиций нельзя изменить снаружи")
    void itemsAreImmutable() {
        assertThatThrownBy(() -> newOrder().items().add(mug))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("После оплаты заказ хранит способ оплаты и комиссию")
    void paysOrder() {
        Payment card = new Payment.Card("**** 4242", false);

        Order paid = newOrder().pay(card, new BigDecimal("85.80"));

        assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
        assertThat(paid.payment()).isEqualTo(card);
        assertThat(paid.commission()).isEqualByComparingTo("85.80");
    }

    @Test
    @DisplayName("Оплаченный заказ нельзя оплатить ещё раз")
    void cannotPayTwice() {
        Order paid = newOrder().pay(new Payment.Sbp("+79161234567"), BigDecimal.ONE);

        assertThatThrownBy(() -> paid.pay(new Payment.Sbp("+79161234567"), BigDecimal.ONE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("оплачен");
    }

    @Test
    @DisplayName("Наличных должно хватить на всю сумму заказа")
    void cashMustCoverTotal() {
        assertThatThrownBy(() -> newOrder().pay(new Payment.Cash(new BigDecimal("5719.99")), BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("к оплате 5720.00");
    }

    @Test
    @DisplayName("Наличные ровно на сумму заказа принимаются (граничный случай)")
    void acceptsExactCash() {
        Order paid = newOrder().pay(new Payment.Cash(new BigDecimal("5720.00")), BigDecimal.ZERO);

        assertThat(paid.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("Отменяет новый заказ")
    void cancelsNewOrder() {
        assertThat(newOrder().cancel().status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("При отмене оплаченного заказа оплата сохраняется")
    void cancelKeepsPayment() {
        Order cancelled = newOrder().pay(new Payment.Sbp("+79161234567"), BigDecimal.ONE).cancel();

        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.payment()).isNotNull();
    }

    @Test
    @DisplayName("Отменённый заказ нельзя ни отменить, ни оплатить")
    void cancelledIsFinal() {
        Order cancelled = newOrder().cancel();

        assertThatThrownBy(cancelled::cancel).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> cancelled.pay(new Payment.Sbp("+79161234567"), BigDecimal.ONE))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("отменён");
    }
}
