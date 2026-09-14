package org.hse.examples.infrastructure;

import org.hse.examples.domain.Order;
import org.hse.examples.domain.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Преобразование заказов и сумм в строки для вывода */
@Component
public class OrderFormatter {

    /** Строка заказа: покупатель, сумма, способ оплаты и комиссия */
    public String format(Order order, BigDecimal commission) {
        String payment = switch (order.payment()) {
            case Payment.Card(var number, var foreignIssuer) ->
                    "карта " + number + (foreignIssuer ? " (зарубежный банк)" : " (российский банк)");
            case Payment.Sbp(var phone) -> "СБП " + phone;
            case Payment.Cash(var received) -> "наличные, получено " + money(received);
        };
        return "№%d %s — %s, оплата: %s, комиссия: %s".formatted(
                order.id(), order.customer(), money(order.amount()), payment, money(commission));
    }

    /** Сумма для вывода (округление до копеек) */
    public String money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP) + " руб.";
    }
}
