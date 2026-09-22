package org.hse.shop.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционный тест отчёта по продажам.
 * Часы зафиксированы на 15 января 2026, а база своя, чтобы в отчёт не попали заказы из других тестов.
 */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:report-test")
@AutoConfigureMockMvc
class ReportApiTest {

    @TestBean
    private Clock clock;

    @Autowired
    private MockMvcTester mvc;

    private ShopApi api;

    static Clock clock() {
        return Clock.fixed(Instant.parse("2026-01-15T09:00:00Z"), ZoneId.of("Europe/Moscow"));
    }

    @BeforeEach
    void setUp() {
        api = new ShopApi(mvc);
    }

    private void pay(long order, String paymentJson) {
        assertThat(api.post("/api/orders/%d/payment".formatted(order), paymentJson)).hasStatusOk();
    }

    @Test
    @DisplayName("Отчёт учитывает только оплаченные заказы и считает топ товаров")
    void salesReport() {
        long customer = api.newCustomer();
        long tea = api.newProduct("590.00", 20);
        long mug = api.newProduct("350.00", 20);

        long bySbp = api.placeOrder(customer, "PICKUP", """
                [{"productId": %d, "quantity": 2}]""".formatted(tea));
        pay(bySbp, """
                {"method": "SBP", "phone": "+79161234567"}""");                            // 1180.00, комиссия 4.72

        long byCash = api.placeOrder(customer, "POST", """
                [{"productId": %d, "quantity": 1}, {"productId": %d, "quantity": 1}]""".formatted(mug, tea));
        pay(byCash, """
                {"method": "CASH", "received": 2000}""");                                  // 1230.00 с почтой

        api.placeOrder(customer, "PICKUP", """
                [{"productId": %d, "quantity": 5}]""".formatted(mug));                   // не оплачен

        long cancelled = api.placeOrder(customer, "PICKUP", """
                [{"productId": %d, "quantity": 1}]""".formatted(tea));
        pay(cancelled, """
                {"method": "CARD", "cardNumber": "1111"}""");
        assertThat(api.post("/api/orders/%d/cancel".formatted(cancelled), "")).hasStatusOk();

        assertThat(mvc.get().uri("/api/reports/sales?from=2026-01-15&to=2026-01-15"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"ordersCount": 2, "revenue": 2410.00, "commission": 4.72, "netRevenue": 2405.28,
                         "averageCheck": 1205.00,
                         "topProducts": [
                           {"productId": %d, "quantity": 3, "amount": 1770.00},
                           {"productId": %d, "quantity": 1, "amount": 350.00}]}""".formatted(tea, mug));
    }

    @Test
    @DisplayName("За день без продаж — пустой отчёт")
    void emptyDay() {
        assertThat(mvc.get().uri("/api/reports/sales?from=2026-01-16&to=2026-01-31"))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"ordersCount": 0, "revenue": 0, "topProducts": []}""");
    }

    @Test
    @DisplayName("Перепутанные границы периода и дата не в том формате — 400")
    void badPeriod() {
        assertThat(mvc.get().uri("/api/reports/sales?from=2026-01-31&to=2026-01-01"))
                .hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(mvc.get().uri("/api/reports/sales?from=15.01.2026&to=2026-01-31"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
