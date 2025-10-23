package com.example.lab3.order.service;
import com.example.lab3.order.model.Customer;
import com.example.lab3.order.model.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public class LoyaltyDiscountService implements DiscountService {

    // ex: 10% pentru clienti loiali
    private final BigDecimal percent = BigDecimal.valueOf(0.10);

    @Override
    public BigDecimal calculateDiscount(Customer customer, Order order) {
        if (customer == null || !customer.isLoyal()) {
            return BigDecimal.ZERO;
        }
        return order.getAmount().multiply(percent);
    }
}