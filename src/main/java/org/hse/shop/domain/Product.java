package org.hse.shop.domain;

import java.math.BigDecimal;

/** Товар в каталоге магазина с остатком на складе */
public record Product(Long id, String name, BigDecimal price, int stock) {

    public Product {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Название товара не может быть пустым");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Цена товара должна быть больше нуля: " + price);
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Остаток не может быть отрицательным: " + stock);
        }
    }

    /** Новый товар без остатка; id назначит хранилище */
    public static Product create(String name, BigDecimal price) {
        return new Product(null, name, price, 0);
    }

    public Product withPrice(BigDecimal newPrice) {
        return new Product(id, name, newPrice, stock);
    }

    /** Товар после поступления на склад */
    public Product receive(int quantity) {
        requirePositive(quantity);
        return new Product(id, name, price, stock + quantity);
    }

    /** Товар после списания со склада */
    public Product take(int quantity) {
        requirePositive(quantity);
        if (quantity > stock) {
            throw new BusinessRuleException("Недостаточно товара «%s»: на складе %d, нужно %d"
                    .formatted(name, stock, quantity));
        }
        return new Product(id, name, price, stock - quantity);
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля: " + quantity);
        }
    }
}
