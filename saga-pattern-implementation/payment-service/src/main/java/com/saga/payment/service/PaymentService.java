package com.saga.payment.service;

import com.saga.payment.dto.OrderDto;
import com.saga.payment.dto.ServiceResponse;
import com.saga.payment.model.Payment;
import com.saga.payment.model.PaymentStatus;
import com.saga.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // COMPENSATABLE TRANSACTION: Reserve payment
    public ServiceResponse reservePayment(OrderDto order) {
        log.info("Reserving payment for Order ID: {}", order.getId());

        try {
            // Simulate payment validation
            if (order.getAmount().doubleValue() > 10000) {
                log.error("Payment amount too high: {}", order.getAmount());
                return new ServiceResponse(false, "Payment amount exceeds limit", null);
            }

            // Check if payment already exists (idempotency)
            if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
                log.warn("Payment already reserved for Order ID: {}", order.getId());
                return new ServiceResponse(true, "Payment already reserved", null);
            }

            // Reserve payment
            Payment payment = new Payment();
            payment.setOrderId(order.getId());
            payment.setCustomerId(order.getCustomerId());
            payment.setAmount(order.getAmount());
            payment.setStatus(PaymentStatus.RESERVED);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);
            log.info("Payment reserved successfully for Order ID: {}", order.getId());

            return new ServiceResponse(true, "Payment reserved successfully", payment);

        } catch (Exception e) {
            log.error("Error reserving payment: {}", e.getMessage());
            return new ServiceResponse(false, "Error reserving payment: " + e.getMessage(), null);
        }
    }

    // COMPENSATION: Refund payment
    public ServiceResponse refundPayment(OrderDto order) {
        log.info("Refunding payment for Order ID: {}", order.getId());

        try {
            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            log.info("Payment refunded successfully for Order ID: {}", order.getId());
            return new ServiceResponse(true, "Payment refunded successfully", payment);

        } catch (Exception e) {
            log.error("Error refunding payment: {}", e.getMessage());
            return new ServiceResponse(false, "Error refunding payment: " + e.getMessage(), null);
        }
    }
}