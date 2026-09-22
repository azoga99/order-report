package org.hse.shop.infrastructure.persistence;

import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderStatus;
import org.hse.shop.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** Тесты хранения заказов в H2: позиции, способы оплаты, выборки. */
@DataJpaTest
@Import(OrderRepositoryAdapter.class)
class OrderRepositoryAdapterTest {

    private static final LocalDateTime SEPT_1 = LocalDateTime.of(2026, 9, 1, 0, 0);

    @Autowired
    private OrderRepositoryAdapter repository;

    private static Order order(long customerId, LocalDateTime createdAt) {
        return Order.create(customerId, List.of(
                        new OrderItem(1L, "Чайник", new BigDecimal("2490.00"), 1),
                        new OrderItem(2L, "Кружка", new BigDecimal("350.00"), 3)),
                DeliveryMethod.COURIER, new BigDecimal("390.00"), createdAt);
    }

    static Stream<Payment> payments() {
        return Stream.of(
                new Payment.Card("**** 4242", true),
                new Payment.Sbp("+79161234567"),
                new Payment.Cash(new BigDecimal("5000.00")));
    }

    @Test
    @DisplayName("Сохраняет новый заказ с позициями и назначает id")
    void savesNewOrder() {
        Order saved = repository.save(order(1L, SEPT_1));

        Order loaded = repository.findById(saved.id()).orElseThrow();

        assertThat(saved.id()).isNotNull();
        assertThat(loaded.items()).extracting(OrderItem::productName).containsExactly("Чайник", "Кружка");
        assertThat(loaded.total()).isEqualByComparingTo("3930.00");
        assertThat(loaded.status()).isEqualTo(OrderStatus.NEW);
        assertThat(loaded.payment()).isNull();
    }

    @ParameterizedTest
    @MethodSource("payments")
    @DisplayName("Сохраняет и восстанавливает каждый способ оплаты")
    void storesPayment(Payment payment) {
        Order saved = repository.save(order(1L, SEPT_1));

        repository.save(saved.pay(payment, new BigDecimal("12.34")));
        Order loaded = repository.findById(saved.id()).orElseThrow();

        assertThat(loaded.status()).isEqualTo(OrderStatus.PAID);
        assertThat(loaded.commission()).isEqualByComparingTo("12.34");
        assertThat(loaded.payment()).usingRecursiveComparison()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(payment);
    }

    @Test
    @DisplayName("Несуществующий заказ не находится")
    void missingOrder() {
        assertThat(repository.findById(999_999L)).isEmpty();
    }

    @Test
    @DisplayName("Находит заказы только нужного покупателя")
    void findsByCustomer() {
        repository.save(order(7L, SEPT_1));
        repository.save(order(7L, SEPT_1));
        repository.save(order(8L, SEPT_1));

        assertThat(repository.findByCustomerId(7L)).hasSize(2).allMatch(o -> o.customerId() == 7L);
    }

    @Test
    @DisplayName("Выборка за период включает начало и не включает конец")
    void findsCreatedBetween() {
        Order atStart = repository.save(order(1L, SEPT_1));
        Order inside = repository.save(order(1L, SEPT_1.plusDays(3)));
        repository.save(order(1L, SEPT_1.plusDays(7)));
        repository.save(order(1L, SEPT_1.minusSeconds(1)));

        List<Order> found = repository.findCreatedBetween(SEPT_1, SEPT_1.plusDays(7));

        assertThat(found).extracting(Order::id).containsExactlyInAnyOrder(atStart.id(), inside.id());
    }
}
