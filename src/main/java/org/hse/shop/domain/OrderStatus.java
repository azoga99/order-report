package org.hse.shop.domain;

/** Статус заказа */
public enum OrderStatus {
    NEW("новый"),
    PAID("оплачен"),
    CANCELLED("отменён");

    private final String title;

    OrderStatus(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
