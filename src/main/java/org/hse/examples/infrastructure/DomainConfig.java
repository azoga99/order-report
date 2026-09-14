package org.hse.examples.infrastructure;

import org.hse.examples.domain.CommissionPolicy;
import org.hse.examples.domain.RateCommissionPolicy;
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
