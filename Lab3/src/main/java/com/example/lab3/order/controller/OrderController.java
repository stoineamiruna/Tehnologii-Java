package com.example.lab3.order.controller;

import com.example.lab3.order.model.Order;
import com.example.lab3.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping("/apply")
    public String applyDiscount(@RequestParam Long customerId, @RequestParam double amount) {
        Order order = new Order(System.currentTimeMillis(), customerId, BigDecimal.valueOf(amount));
        var discount = orderService.applyDiscount(order);
        return "Discount applied: " + discount;
    }
}