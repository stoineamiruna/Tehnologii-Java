package com.example.lab3.order.event;

import com.example.lab3.order.model.Customer;
import com.example.lab3.order.model.Order;

import java.math.BigDecimal;

public class LargeDiscountEvent {
    private final Object source;
    private final Order order;
    private final Customer customer;
    private final BigDecimal discount;

    public LargeDiscountEvent(Object source, Order order, Customer customer, BigDecimal discount) {
        this.source = source;
        this.order = order;
        this.customer = customer;
        this.discount = discount;
    }

    public Object getSource() { return source; }
    public Order getOrder() { return order; }
    public Customer getCustomer() { return customer; }
    public BigDecimal getDiscount() { return discount; }
}