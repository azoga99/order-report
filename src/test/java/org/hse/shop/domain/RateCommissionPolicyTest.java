package org.hse.shop.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/** Тесты расчёта комиссии по фиксированным ставкам. */
class RateCommissionPolicyTest {

    private final RateCommissionPolicy policy = new RateCommissionPolicy();
    private final BigDecimal thousand = new BigDecimal("1000.00");

    @Test
    @DisplayName("Карта российского банка — 1.5%")
    void domesticCard() {
        assertThat(policy.commissionFor(thousand, new Payment.Card("**** 1111", false)))
                .isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("Карта зарубежного банка — 3.5%")
    void foreignCard() {
        assertThat(policy.commissionFor(thousand, new Payment.Card("**** 2222", true)))
                .isEqualByComparingTo("35.00");
    }

    @Test
    @DisplayName("СБП — 0.4%")
    void sbp() {
        assertThat(policy.commissionFor(thousand, new Payment.Sbp("+79161234567")))
                .isEqualByComparingTo("4.00");
    }

    @Test
    @DisplayName("Наличные — без комиссии")
    void cash() {
        assertThat(policy.commissionFor(thousand, new Payment.Cash(thousand))).isZero();
    }

    @Test
    @DisplayName("Округляет комиссию до копеек")
    void roundsToKopecks() {
        BigDecimal commission = policy.commissionFor(new BigDecimal("11191.00"), new Payment.Card("**** 4242", false));

        assertThat(commission).isEqualTo(new BigDecimal("167.87"));
    }

    @Test
    @DisplayName("Нулевая сумма даёт нулевую комиссию (граничный случай)")
    void zeroAmount() {
        assertThat(policy.commissionFor(BigDecimal.ZERO, new Payment.Card("**** 3333", true))).isZero();
    }
}
