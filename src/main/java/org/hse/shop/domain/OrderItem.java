package org.hse.shop.domain;

import java.math.BigDecimal;

/** Позиция заказа. Цена запоминается на момент оформления и не меняется вместе с каталогом */
public record OrderItem(long productId, String productName, BigDecimal price, int quantity) {

    public OrderItem {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Цена позиции должна быть больше нуля: " + price);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля: " + quantity);
        }
    }

    /** Стоимость позиции: цена × количество */
    public BigDecimal amount() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
