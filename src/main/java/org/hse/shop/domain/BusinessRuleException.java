package org.hse.shop.domain;

/** Операция нарушает правила магазина: не хватает товара, заказ уже оплачен и т. п. */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
