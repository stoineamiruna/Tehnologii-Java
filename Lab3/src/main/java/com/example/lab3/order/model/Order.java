package com.example.lab3.order.model;
import java.math.BigDecimal;

public class Order {
    private final Long id;
    private final Long customerId;
    private final BigDecimal amount;

    public Order(Long id, Long customerId, BigDecimal amount) {
        this.id = id;
        this.customerId = customerId;
        this.amount = amount;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
}
