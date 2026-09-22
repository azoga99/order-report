package org.hse.shop.domain;

import java.math.BigDecimal;

/** Способ оплаты заказа */
public sealed interface Payment {

    record Card(String maskedNumber, boolean foreignIssuer) implements Payment {
    }

    record Sbp(String phone) implements Payment {
    }

    record Cash(BigDecimal received) implements Payment {
    }
}
