package org.hse.shop.infrastructure.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

/** Интеграционные тесты API заказов: от оформления до отмены. */
@SpringBootTest
@AutoConfigureMockMvc
class OrderApiTest {

    @Autowired
    private MockMvcTester mvc;

    private ShopApi api;

    @BeforeEach
    void setUp() {
        api = new ShopApi(mvc);
    }

    @Test
    @DisplayName("Заказ проходит путь: оформление → оплата картой → история → отмена с возвратом")
    void orderLifecycle() {
        long customer = api.newCustomer();
        long kettle = api.newProduct("2490.00", 10);

        long order = api.placeOrder(customer, "COURIER", """
                [{"productId": %d, "quantity": 3}]""".formatted(kettle));
        assertThat(mvc.get().uri("/api/orders/{id}", order))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"status": "NEW", "itemsTotal": 7470.00, "deliveryCost": 0, "total": 7470.00,
                         "payment": null}""");
        assertThat(api.stockOf(kettle)).isEqualTo(7);

        assertThat(api.post("/api/orders/%d/payment".formatted(order), """
                {"method": "CARD", "cardNumber": "4242 4242 4242 4242", "foreignIssuer": false}"""))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"status": "PAID", "payment": {"method": "CARD", "cardNumber": "**** 4242"},
                         "commission": 112.05}""");

        assertThat(api.post("/api/orders/%d/payment".formatted(order), """
                {"method": "SBP", "phone": "+79161234567"}"""))
                .hasStatus(HttpStatus.CONFLICT);

        assertThat(mvc.get().uri("/api/customers/{id}/orders", customer))
                .hasStatusOk()
                .bodyJson().extractingPath("$[*].id").asArray().containsExactly((int) order);

        assertThat(api.post("/api/orders/%d/cancel".formatted(order), ""))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"order": {"status": "CANCELLED"}, "refund": 7470.00}""");
        assertThat(api.stockOf(kettle)).isEqualTo(10);

        assertThat(api.post("/api/orders/%d/cancel".formatted(order), ""))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("Если одного товара не хватает, списание других тоже откатывается")
    void rollsBackReservations() {
        long customer = api.newCustomer();
        long plenty = api.newProduct("100.00", 5);
        long scarce = api.newProduct("200.00", 1);

        assertThat(api.post("/api/orders", """
                {"customerId": %d, "delivery": "PICKUP",
                 "items": [{"productId": %d, "quantity": 2}, {"productId": %d, "quantity": 3}]}"""
                .formatted(customer, plenty, scarce)))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.detail").asString().contains("Недостаточно товара");

        assertThat(api.stockOf(plenty)).isEqualTo(5);
        assertThat(api.stockOf(scarce)).isEqualTo(1);
    }

    @Test
    @DisplayName("Наличных меньше суммы заказа — 409, заказ остаётся неоплаченным")
    void notEnoughCash() {
        long order = api.placeOrder(api.newCustomer(), "PICKUP", """
                [{"productId": %d, "quantity": 1}]""".formatted(api.newProduct("500.00", 3)));

        assertThat(api.post("/api/orders/%d/payment".formatted(order), """
                {"method": "CASH", "received": 400}"""))
                .hasStatus(HttpStatus.CONFLICT);
        assertThat(mvc.get().uri("/api/orders/{id}", order))
                .bodyJson().extractingPath("$.status").isEqualTo("NEW");
    }

    @Test
    @DisplayName("Оплата картой без номера — 400 с названием поля")
    void cardWithoutNumber() {
        long order = api.placeOrder(api.newCustomer(), "PICKUP", """
                [{"productId": %d, "quantity": 1}]""".formatted(api.newProduct("500.00", 3)));

        assertThat(api.post("/api/orders/%d/payment".formatted(order), """
                {"method": "CARD"}"""))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").asString().contains("cardNumber");
    }

    @Test
    @DisplayName("Пустой заказ и неизвестный способ доставки — 400")
    void invalidOrderRequests() {
        long customer = api.newCustomer();

        assertThat(api.post("/api/orders", """
                {"customerId": %d, "delivery": "PICKUP", "items": []}""".formatted(customer)))
                .hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(api.post("/api/orders", """
                {"customerId": %d, "delivery": "DRONE", "items": [{"productId": 1, "quantity": 1}]}"""
                .formatted(customer)))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("Заказ для неизвестного покупателя и несуществующий заказ — 404")
    void notFound() {
        assertThat(api.post("/api/orders", """
                {"customerId": 999999, "delivery": "PICKUP", "items": [{"productId": 1, "quantity": 1}]}"""))
                .hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.get().uri("/api/orders/999999")).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(api.post("/api/orders/999999/cancel", "")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Считает доставку заранее, до оформления заказа")
    void deliveryCost() {
        assertThat(mvc.get().uri("/api/delivery/cost?method=COURIER&itemsTotal=4999.99"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.cost").isEqualTo(390.0);
        assertThat(mvc.get().uri("/api/delivery/cost?method=COURIER&itemsTotal=5000"))
                .bodyJson().extractingPath("$.cost").isEqualTo(0);
        assertThat(mvc.get().uri("/api/delivery/cost?method=DRONE&itemsTotal=100"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
