package com.example.lab3.order.service;

import com.example.lab3.order.model.Customer;
import com.example.lab3.order.model.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public class LargeOrderDiscountService implements DiscountService {

    private final BigDecimal threshold = BigDecimal.valueOf(500); // prag
    private final BigDecimal fixedDiscount = BigDecimal.valueOf(50); // discount fix

    @Override
    public BigDecimal calculateDiscount(Customer customer, Order order) {
        if (order.getAmount().compareTo(threshold) >= 0) {
            return fixedDiscount;
        }
        return BigDecimal.ZERO;
    }
}