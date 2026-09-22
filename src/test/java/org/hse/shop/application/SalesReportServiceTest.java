package org.hse.shop.application;

import org.hse.shop.application.SalesReport.ProductSales;
import org.hse.shop.domain.DeliveryMethod;
import org.hse.shop.domain.Order;
import org.hse.shop.domain.OrderItem;
import org.hse.shop.domain.OrderRepository;
import org.hse.shop.domain.Payment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Тесты отчёта по продажам. */
@ExtendWith(MockitoExtension.class)
class SalesReportServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 9, 1);
    private static final LocalDate TO = LocalDate.of(2026, 9, 30);

    @Mock
    private OrderRepository orders;

    @InjectMocks
    private SalesReportService service;

    private static OrderItem kettles(int quantity) {
        return new OrderItem(1L, "Чайник", new BigDecimal("2490.00"), quantity);
    }

    private static OrderItem mugs(int quantity) {
        return new OrderItem(2L, "Кружка", new BigDecimal("350.00"), quantity);
    }

    private static Order order(List<OrderItem> items, BigDecimal deliveryCost) {
        return Order.create(1L, items, DeliveryMethod.COURIER, deliveryCost, LocalDateTime.of(2026, 9, 10, 12, 0));
    }

    private static Order paid(Order order, String commission) {
        return order.pay(new Payment.Card("**** 4242", false), new BigDecimal(commission));
    }

    @Test
    @DisplayName("Считает выручку с доставкой, комиссию, чистую выручку и средний чек")
    void calculatesTotals() {
        when(orders.findCreatedBetween(any(), any())).thenReturn(List.of(
                paid(order(List.of(kettles(1)), new BigDecimal("390.00")), "43.20"),     // 2880.00
                paid(order(List.of(mugs(3)), new BigDecimal("390.00")), "21.60")));      // 1440.00

        SalesReport report = service.build(FROM, TO);

        assertThat(report.ordersCount()).isEqualTo(2);
        assertThat(report.revenue()).isEqualByComparingTo("4320.00");
        assertThat(report.commission()).isEqualByComparingTo("64.80");
        assertThat(report.netRevenue()).isEqualByComparingTo("4255.20");
        assertThat(report.averageCheck()).isEqualByComparingTo("2160.00");
    }

    @Test
    @DisplayName("Учитывает только оплаченные заказы: новые и отменённые не входят в отчёт")
    void countsOnlyPaidOrders() {
        Order paidOrder = paid(order(List.of(kettles(1)), BigDecimal.ZERO), "37.35");
        when(orders.findCreatedBetween(any(), any())).thenReturn(List.of(
                paidOrder,
                order(List.of(kettles(5)), BigDecimal.ZERO),
                paid(order(List.of(mugs(1)), BigDecimal.ZERO), "5.25").cancel()));

        SalesReport report = service.build(FROM, TO);

        assertThat(report.ordersCount()).isEqualTo(1);
        assertThat(report.revenue()).isEqualByComparingTo("2490.00");
        assertThat(report.topProducts()).extracting(ProductSales::name).containsExactly("Чайник");
    }

    @Test
    @DisplayName("Складывает продажи одного товара из разных заказов и сортирует по выручке")
    void topProductsByAmount() {
        when(orders.findCreatedBetween(any(), any())).thenReturn(List.of(
                paid(order(List.of(mugs(4), kettles(1)), BigDecimal.ZERO), "0"),
                paid(order(List.of(kettles(2)), BigDecimal.ZERO), "0")));

        SalesReport report = service.build(FROM, TO);

        assertThat(report.topProducts()).containsExactly(
                new ProductSales(1L, "Чайник", 3, new BigDecimal("7470.00")),
                new ProductSales(2L, "Кружка", 4, new BigDecimal("1400.00")));
    }

    @Test
    @DisplayName("В топе не больше пяти товаров")
    void topIsLimited() {
        List<OrderItem> items = IntStream.rangeClosed(1, 7)
                .mapToObj(id -> new OrderItem(id, "Товар " + id, new BigDecimal(id * 100), 1))
                .toList();
        when(orders.findCreatedBetween(any(), any())).thenReturn(List.of(paid(order(items, BigDecimal.ZERO), "0")));

        SalesReport report = service.build(FROM, TO);

        assertThat(report.topProducts()).extracting(ProductSales::productId).containsExactly(7L, 6L, 5L, 4L, 3L);
    }

    @Test
    @DisplayName("За период без продаж — нули и пустой топ (граничный случай)")
    void emptyPeriod() {
        when(orders.findCreatedBetween(any(), any())).thenReturn(List.of());

        SalesReport report = service.build(FROM, TO);

        assertThat(report.ordersCount()).isZero();
        assertThat(report.revenue()).isZero();
        assertThat(report.averageCheck()).isZero();
        assertThat(report.topProducts()).isEmpty();
    }

    @Test
    @DisplayName("Последний день периода входит в отчёт целиком")
    void includesLastDay() {
        when(orders.findCreatedBetween(any(), any())).thenReturn(List.of());

        service.build(FROM, TO);

        verify(orders).findCreatedBetween(LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTime.of(2026, 10, 1, 0, 0));
    }

    @Test
    @DisplayName("Отчёт за один день (граничный случай)")
    void singleDay() {
        when(orders.findCreatedBetween(any(), any())).thenReturn(List.of());

        assertThat(service.build(FROM, FROM).ordersCount()).isZero();
        verify(orders).findCreatedBetween(LocalDateTime.of(2026, 9, 1, 0, 0), LocalDateTime.of(2026, 9, 2, 0, 0));
    }

    @Test
    @DisplayName("Отклоняет период, у которого конец раньше начала")
    void rejectsInvertedPeriod() {
        assertThatThrownBy(() -> service.build(TO, FROM))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("раньше начала");
        verifyNoInteractions(orders);
    }
}
