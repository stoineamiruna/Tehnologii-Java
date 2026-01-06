package com.example.orderhistoryservice.controller;

import com.example.orderhistoryservice.document.OrderHistory;
import com.example.orderhistoryservice.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-history")
@RequiredArgsConstructor
public class OrderHistoryController {

    private final OrderHistoryRepository orderHistoryRepository;

    @GetMapping
    public ResponseEntity<List<OrderHistory>> getAllOrderHistory() {
        List<OrderHistory> history = orderHistoryRepository.findAll();
        return ResponseEntity.ok(history);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderHistory>> getOrderHistoryByUserId(@PathVariable Long userId) {
        List<OrderHistory> history = orderHistoryRepository.findByUserId(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<OrderHistory> getOrderHistoryByOrderId(@PathVariable Long orderId) {
        return orderHistoryRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}