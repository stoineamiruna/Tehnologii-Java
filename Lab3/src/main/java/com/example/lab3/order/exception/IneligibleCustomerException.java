package com.example.lab3.order.exception;

public class IneligibleCustomerException extends RuntimeException {
    public IneligibleCustomerException(String message) {
        super(message);
    }
}
