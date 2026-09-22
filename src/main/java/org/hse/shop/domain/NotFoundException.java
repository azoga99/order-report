package org.hse.shop.domain;

/** Объект с указанным идентификатором не найден */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException product(long id) {
        return new NotFoundException("Товар не найден: " + id);
    }

    public static NotFoundException customer(long id) {
        return new NotFoundException("Покупатель не найден: " + id);
    }

    public static NotFoundException order(long id) {
        return new NotFoundException("Заказ не найден: " + id);
    }
}
