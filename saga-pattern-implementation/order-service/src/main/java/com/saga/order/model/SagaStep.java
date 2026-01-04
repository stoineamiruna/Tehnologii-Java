package com.saga.order.model;

public enum SagaStep {
    CREATE_ORDER,
    RESERVE_PAYMENT,
    RESERVE_INVENTORY,
    SHIP_ORDER,
    SEND_NOTIFICATION
}