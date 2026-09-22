package org.hse.shop.infrastructure.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** Интеграционные тесты API каталога: настоящий контекст Spring и база H2. */
@SpringBootTest
@AutoConfigureMockMvc
class ProductApiTest {

    @Autowired
    private MockMvcTester mvc;

    private long addProduct(String name, String price) throws Exception {
        MvcTestResult result = mvc.post().uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "%s", "price": %s}""".formatted(name, price))
                .exchange();
        assertThat(result).hasStatus(HttpStatus.CREATED);
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id"))
                .longValue();
    }

    @Test
    @DisplayName("Отдаёт демонстрационный каталог")
    void listsDemoCatalog() {
        assertThat(mvc.get().uri("/api/products"))
                .hasStatusOk()
                .bodyJson().extractingPath("$[*].name").asArray().contains("Чайник электрический");
    }

    @Test
    @DisplayName("Добавляет товар без остатка и находит его по id")
    void addsProduct() throws Exception {
        long id = addProduct("Турка медная", "990.00");

        assertThat(mvc.get().uri("/api/products/{id}", id))
                .hasStatusOk()
                .bodyJson().isLenientlyEqualTo("""
                        {"id": %d, "name": "Турка медная", "price": 990.00, "stock": 0}""".formatted(id));
    }

    @Test
    @DisplayName("Товар без остатка не попадает в список «в наличии»")
    void inStockFilter() throws Exception {
        long id = addProduct("Сито для чая", "150.00");

        assertThat(mvc.get().uri("/api/products?inStock=true"))
                .hasStatusOk()
                .bodyJson().extractingPath("$[*].id").asArray().doesNotContain((int) id);
    }

    @Test
    @DisplayName("Поступление увеличивает остаток, смена цены сохраняется")
    void supplyAndPrice() throws Exception {
        long id = addProduct("Термос", "1500.00");

        assertThat(mvc.post().uri("/api/products/{id}/supply", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantity": 12}"""))
                .hasStatusOk()
                .bodyJson().extractingPath("$.stock").isEqualTo(12);

        assertThat(mvc.put().uri("/api/products/{id}/price", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"price": 1350.00}"""))
                .hasStatusOk()
                .bodyJson().extractingPath("$.price").isEqualTo(1350.0);
    }

    @Test
    @DisplayName("Несуществующий товар — 404 в формате Problem Details")
    void notFound() {
        assertThat(mvc.get().uri("/api/products/999999"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.detail").isEqualTo("Товар не найден: 999999");
    }

    @Test
    @DisplayName("Некорректное тело запроса — 400 со списком полей")
    void validationError() {
        assertThat(mvc.post().uri("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "", "price": -5}"""))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").asString().contains("name:", "price:");
    }

    @Test
    @DisplayName("Нулевое поступление — 400 (граничный случай)")
    void zeroSupply() {
        assertThat(mvc.post().uri("/api/products/1/supply")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"quantity": 0}"""))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
