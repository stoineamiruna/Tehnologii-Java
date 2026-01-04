package com.saga.payment.controller;

import com.saga.payment.dto.OrderDto;
import com.saga.payment.dto.ServiceResponse;
import com.saga.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/reserve")
    public ResponseEntity<ServiceResponse> reservePayment(@RequestBody OrderDto order) {
        ServiceResponse response = paymentService.reservePayment(order);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refund")
    public ResponseEntity<ServiceResponse> refundPayment(@RequestBody OrderDto order) {
        ServiceResponse response = paymentService.refundPayment(order);
        return ResponseEntity.ok(response);
    }
}
