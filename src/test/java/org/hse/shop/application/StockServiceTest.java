package org.hse.shop.application;

import org.hse.shop.domain.BusinessRuleException;
import org.hse.shop.domain.NotFoundException;
import org.hse.shop.domain.Product;
import org.hse.shop.domain.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Тесты движения товара по складу. */
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProductCatalogService catalog;

    @Mock
    private ProductRepository products;

    @InjectMocks
    private StockService stock;

    private final Product kettle = new Product(1L, "Чайник", new BigDecimal("2490.00"), 5);

    @BeforeEach
    void setUp() {
        lenient().when(catalog.get(1L)).thenReturn(kettle);
        lenient().when(products.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    @DisplayName("Поступление увеличивает остаток")
    void receives() {
        assertThat(stock.receive(1L, 10).stock()).isEqualTo(15);
    }

    @Test
    @DisplayName("Резерв под заказ уменьшает остаток")
    void reserves() {
        assertThat(stock.reserve(1L, 2).stock()).isEqualTo(3);
        verify(products).save(kettle.take(2));
    }

    @Test
    @DisplayName("Не резервирует больше, чем есть, и ничего не сохраняет")
    void refusesToReserveTooMuch() {
        assertThatThrownBy(() -> stock.reserve(1L, 6))
                .isInstanceOf(BusinessRuleException.class);

        verify(products, never()).save(any());
    }

    @Test
    @DisplayName("Возврат после отмены увеличивает остаток")
    void releases() {
        assertThat(stock.release(1L, 2).stock()).isEqualTo(7);
    }

    @Test
    @DisplayName("Ошибка, если товара нет в каталоге")
    void failsOnMissingProduct() {
        when(catalog.get(99L)).thenThrow(NotFoundException.product(99L));

        assertThatThrownBy(() -> stock.receive(99L, 1)).isInstanceOf(NotFoundException.class);
    }
}
