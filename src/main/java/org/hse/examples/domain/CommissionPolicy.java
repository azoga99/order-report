package org.hse.examples.domain;

import java.math.BigDecimal;

/** Правило расчёта комиссии платёжной системы */
public interface CommissionPolicy {

    BigDecimal commissionFor(Order order);
}
