package org.hse.shop.infrastructure.web;

import jakarta.validation.constraints.NotNull;
import org.hse.shop.domain.Payment;

import java.math.BigDecimal;

/**
 * Способ оплаты в запросах и ответах API. Нужные поля зависят от method:
 * CARD — cardNumber и foreignIssuer, SBP — phone, CASH — received.
 * От номера карты сохраняются только последние четыре цифры.
 */
public record PaymentDto(@NotNull Method method, String cardNumber, Boolean foreignIssuer, String phone,
                         BigDecimal received) {

    public enum Method { CARD, SBP, CASH }

    Payment toPayment() {
        return switch (method) {
            case CARD -> new Payment.Card(mask(require(cardNumber, "cardNumber")), Boolean.TRUE.equals(foreignIssuer));
            case SBP -> new Payment.Sbp(require(phone, "phone"));
            case CASH -> new Payment.Cash(require(received, "received"));
        };
    }

    static PaymentDto from(Payment payment) {
        return switch (payment) {
            case null -> null;
            case Payment.Card(var number, var foreign) -> new PaymentDto(Method.CARD, number, foreign, null, null);
            case Payment.Sbp(var phone) -> new PaymentDto(Method.SBP, null, null, phone, null);
            case Payment.Cash(var received) -> new PaymentDto(Method.CASH, null, null, null, received);
        };
    }

    private <T> T require(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("Для оплаты " + method + " нужно поле " + field);
        }
        return value;
    }

    private static String mask(String cardNumber) {
        String digits = cardNumber.replaceAll("[^0-9]", "");
        if (digits.length() < 4) {
            throw new IllegalArgumentException("В номере карты должно быть хотя бы 4 цифры");
        }
        return "**** " + digits.substring(digits.length() - 4);
    }
}
