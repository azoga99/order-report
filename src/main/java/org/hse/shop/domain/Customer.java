package org.hse.shop.domain;

import java.util.regex.Pattern;

/** Покупатель магазина */
public record Customer(Long id, String name, String phone) {

    private static final Pattern PHONE = Pattern.compile("\\+7\\d{10}");

    public Customer {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя покупателя не может быть пустым");
        }
        if (phone == null || !PHONE.matcher(phone).matches()) {
            throw new IllegalArgumentException("Телефон должен быть в формате +7XXXXXXXXXX: " + phone);
        }
    }

    /** Новый покупатель; id назначит хранилище */
    public static Customer create(String name, String phone) {
        return new Customer(null, name, phone);
    }
}
