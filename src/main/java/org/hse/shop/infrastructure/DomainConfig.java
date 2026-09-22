package org.hse.shop.infrastructure;

import org.hse.shop.domain.CommissionPolicy;
import org.hse.shop.domain.RateCommissionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Регистрация доменных объектов в контексте Spring, чтобы в самом домене не было аннотаций */
@Configuration
public class DomainConfig {

    @Bean
    public CommissionPolicy commissionPolicy() {
        return new RateCommissionPolicy();
    }
}
