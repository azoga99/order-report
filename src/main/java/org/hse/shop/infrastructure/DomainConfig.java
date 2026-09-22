package org.hse.shop.infrastructure;

import org.hse.shop.domain.CommissionPolicy;
import org.hse.shop.domain.RateCommissionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Регистрация в контексте Spring объектов без аннотаций: доменной политики комиссий,
 * чтобы в самом домене не было Spring, и системных часов, чтобы в тестах время можно было зафиксировать.
 */
@Configuration
public class DomainConfig {

    @Bean
    public CommissionPolicy commissionPolicy() {
        return new RateCommissionPolicy();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
