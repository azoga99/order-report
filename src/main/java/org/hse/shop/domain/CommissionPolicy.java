package org.hse.shop.domain;

import java.math.BigDecimal;

/** Правило расчёта комиссии платёжной системы */
public interface CommissionPolicy {

    BigDecimal commissionFor(BigDecimal amount, Payment payment);
}
