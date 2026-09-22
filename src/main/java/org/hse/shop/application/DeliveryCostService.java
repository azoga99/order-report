package org.hse.shop.application;

import org.hse.shop.domain.DeliveryMethod;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Стоимость доставки по тарифам магазина:
 * самовывоз бесплатно, почта 290 руб., курьер 390 руб. и бесплатно при заказе от 5000 руб.
 */
@Service
public class DeliveryCostService {

    private static final BigDecimal POST_PRICE = new BigDecimal("290.00");
    private static final BigDecimal COURIER_PRICE = new BigDecimal("390.00");
    private static final BigDecimal FREE_COURIER_FROM = new BigDecimal("5000.00");

    /** Стоимость доставки для заказа с указанной суммой товаров */
    public BigDecimal costFor(DeliveryMethod method, BigDecimal itemsTotal) {
        if (itemsTotal.signum() < 0) {
            throw new IllegalArgumentException("Сумма товаров не может быть отрицательной: " + itemsTotal);
        }
        return switch (method) {
            case PICKUP -> BigDecimal.ZERO;
            case POST -> POST_PRICE;
            case COURIER -> itemsTotal.compareTo(FREE_COURIER_FROM) >= 0 ? BigDecimal.ZERO : COURIER_PRICE;
        };
    }
}
