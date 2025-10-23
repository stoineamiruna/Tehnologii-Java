package com.example.lab3.order.service;
import com.example.lab3.order.model.Customer;
import com.example.lab3.order.model.Order;

import java.math.BigDecimal;

public interface DiscountService {
    /**
     * Calculate discount amount (positive value) for given order/customer.
     * Returns BigDecimal.ZERO if no discount.
     */
    BigDecimal calculateDiscount(Customer customer, Order order);
}