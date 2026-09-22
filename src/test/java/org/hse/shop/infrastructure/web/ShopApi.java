package org.hse.shop.infrastructure.web;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/** Общие шаги API-тестов: завести покупателя и товар с остатком, отправить запрос, прочитать поле ответа. */
class ShopApi {

    /** Каждый тест регистрирует покупателя с новым телефоном, чтобы не упереться в уникальность */
    private static final AtomicInteger PHONE_SUFFIX = new AtomicInteger(1_000_000);

    private final MockMvcTester mvc;

    ShopApi(MockMvcTester mvc) {
        this.mvc = mvc;
    }

    MvcTestResult post(String uri, String json) {
        return mvc.post().uri(uri).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    long newCustomer() {
        MvcTestResult result = post("/api/customers", """
                {"name": "Тестовый покупатель", "phone": "+7999%d"}""".formatted(PHONE_SUFFIX.incrementAndGet()));
        assertThat(result).hasStatus(HttpStatus.CREATED);
        return id(result);
    }

    long newProduct(String price, int stock) {
        MvcTestResult created = post("/api/products", """
                {"name": "Товар за %s", "price": %s}""".formatted(price, price));
        assertThat(created).hasStatus(HttpStatus.CREATED);
        long id = id(created);
        assertThat(post("/api/products/" + id + "/supply", """
                {"quantity": %d}""".formatted(stock))).hasStatusOk();
        return id;
    }

    long placeOrder(long customerId, String delivery, String itemsJson) {
        MvcTestResult result = post("/api/orders", """
                {"customerId": %d, "delivery": "%s", "items": %s}""".formatted(customerId, delivery, itemsJson));
        assertThat(result).hasStatus(HttpStatus.CREATED);
        return id(result);
    }

    int stockOf(long productId) {
        return read(mvc.get().uri("/api/products/{id}", productId).exchange(), "$.stock");
    }

    long id(MvcTestResult result) {
        return ((Number) read(result, "$.id")).longValue();
    }

    static <T> T read(MvcTestResult result, String path) {
        try {
            return JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), path);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
