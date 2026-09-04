package org.hse.examples;

import java.math.BigDecimal;

/** Правило расчёта комиссии платёжной системы */
public interface CommissionPolicy {

    BigDecimal commissionFor(Order order);
}
