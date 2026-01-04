package com.saga.order.model;

public enum OrderStatus {
    PENDING,
    PAYMENT_RESERVED,
    INVENTORY_RESERVED,
    SHIPPED,
    COMPLETED,
    CANCELLED,
    FAILED
}