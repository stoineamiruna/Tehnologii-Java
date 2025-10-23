package com.example.lab3.order.service;

import com.example.lab3.order.model.Customer;
import com.example.lab3.order.model.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

public class NoDiscountService implements DiscountService {
    @Override
    public BigDecimal calculateDiscount(Customer customer, Order order) {
        return BigDecimal.ZERO;
    }
}