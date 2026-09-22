package org.hse.shop.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

/** Интеграционные тесты API покупателей. */
@SpringBootTest
@AutoConfigureMockMvc
class CustomerApiTest {

    @Autowired
    private MockMvcTester mvc;

    private MockMvcTester.MockMvcRequestBuilder register(String name, String phone) {
        return mvc.post().uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "%s", "phone": "%s"}""".formatted(name, phone));
    }

    @Test
    @DisplayName("Регистрирует покупателя")
    void registers() {
        assertThat(register("Игорь Лебедев", "+79265550011"))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson().extractingPath("$.phone").isEqualTo("+79265550011");
    }

    @Test
    @DisplayName("Второй покупатель с тем же телефоном — 409")
    void duplicatePhone() {
        assertThat(register("Ольга Иванова", "+79265550022")).hasStatus(HttpStatus.CREATED);

        assertThat(register("Ольга Петрова", "+79265550022"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.detail").asString().contains("уже зарегистрирован");
    }

    @Test
    @DisplayName("Телефон не в формате +7XXXXXXXXXX — 400")
    void wrongPhone() {
        assertThat(register("Ольга Иванова", "8 926 555-00-33"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.detail").asString().contains("+7XXXXXXXXXX");
    }

    @Test
    @DisplayName("Находит демонстрационного покупателя и сообщает об отсутствующем")
    void getsCustomer() {
        assertThat(mvc.get().uri("/api/customers/1"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.name").isEqualTo("Алексей Гусев");

        assertThat(mvc.get().uri("/api/customers/999999")).hasStatus(HttpStatus.NOT_FOUND);
    }
}
