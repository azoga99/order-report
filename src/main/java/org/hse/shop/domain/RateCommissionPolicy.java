package org.hse.shop.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Комиссия по фиксированным ставкам, округлённая до копеек */
public class RateCommissionPolicy implements CommissionPolicy {

    @Override
    public BigDecimal commissionFor(BigDecimal amount, Payment payment) {
        BigDecimal rate = switch (payment) {
            case Payment.Card card when card.foreignIssuer() -> new BigDecimal("0.035");
            case Payment.Card card -> new BigDecimal("0.015");
            case Payment.Sbp sbp -> new BigDecimal("0.004");
            case Payment.Cash cash -> BigDecimal.ZERO;
        };
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
