package org.hse.examples;

import java.math.BigDecimal;

/** Комиссия по фиксированным ставкам */
public class RateCommissionPolicy implements CommissionPolicy {

    @Override
    public BigDecimal commissionFor(Order order) {
        BigDecimal rate = switch (order.payment()) {
            case Payment.Card card when card.foreignIssuer() -> new BigDecimal("0.035");
            case Payment.Card card -> new BigDecimal("0.015");
            case Payment.Sbp sbp -> new BigDecimal("0.004");
            case Payment.Cash cash -> BigDecimal.ZERO;
        };
        return order.amount().multiply(rate);
    }
}
