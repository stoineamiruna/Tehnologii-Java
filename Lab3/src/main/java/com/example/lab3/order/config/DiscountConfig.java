package com.example.lab3.order.config;

import com.example.lab3.order.service.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscountConfig {

    @Bean
    @ConditionalOnProperty(name = "app.discount.strategy", havingValue = "loyal", matchIfMissing = false)
    public DiscountService loyaltyDiscountService() {
        return new LoyaltyDiscountService();
    }

    @Bean
    @ConditionalOnProperty(name = "app.discount.strategy", havingValue = "large", matchIfMissing = false)
    public DiscountService largeOrderDiscountService() {
        return new LargeOrderDiscountService();
    }

    @Bean
    @ConditionalOnProperty(name = "app.discount.strategy", havingValue = "none", matchIfMissing = true)
    public DiscountService noDiscountService() {
        return new NoDiscountService();
    }
}
