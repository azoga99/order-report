package org.hse.shop.application;

import org.hse.shop.domain.NotFoundException;
import org.hse.shop.domain.Product;
import org.hse.shop.domain.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Тесты каталога товаров. */
@ExtendWith(MockitoExtension.class)
class ProductCatalogServiceTest {

    @Mock
    private ProductRepository products;

    @InjectMocks
    private ProductCatalogService catalog;

    private final Product kettle = new Product(1L, "Чайник", new BigDecimal("2490.00"), 5);

    @Test
    @DisplayName("Добавляет товар без остатка")
    void addsProduct() {
        when(products.save(any())).thenAnswer(call -> call.getArgument(0));

        Product added = catalog.add("Чайник", new BigDecimal("2490.00"));

        assertThat(added.stock()).isZero();
        verify(products).save(added);
    }

    @Test
    @DisplayName("Не сохраняет товар с некорректной ценой")
    void doesNotSaveInvalidProduct() {
        assertThatThrownBy(() -> catalog.add("Чайник", new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(products, never()).save(any());
    }

    @Test
    @DisplayName("Находит товар по id")
    void getsProduct() {
        when(products.findById(1L)).thenReturn(Optional.of(kettle));

        assertThat(catalog.get(1L)).isEqualTo(kettle);
    }

    @Test
    @DisplayName("Сообщает, что товар не найден")
    void failsOnMissingProduct() {
        when(products.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalog.get(42L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("В наличии — только товары с ненулевым остатком")
    void filtersInStock() {
        Product soldOut = new Product(2L, "Кружка", new BigDecimal("350.00"), 0);
        when(products.findAll()).thenReturn(List.of(kettle, soldOut));

        assertThat(catalog.inStock()).containsExactly(kettle);
        assertThat(catalog.list()).containsExactly(kettle, soldOut);
    }

    @Test
    @DisplayName("Меняет цену и сохраняет товар")
    void changesPrice() {
        when(products.findById(1L)).thenReturn(Optional.of(kettle));
        when(products.save(any())).thenAnswer(call -> call.getArgument(0));

        Product changed = catalog.changePrice(1L, new BigDecimal("1990.00"));

        assertThat(changed.price()).isEqualByComparingTo("1990.00");
        assertThat(changed.stock()).isEqualTo(5);
    }
}
