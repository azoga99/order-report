package org.hse.shop.application;

/** Строка корзины при оформлении заказа: какой товар и сколько штук */
public record OrderLine(long productId, int quantity) {

    public OrderLine {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля: " + quantity);
        }
    }
}
