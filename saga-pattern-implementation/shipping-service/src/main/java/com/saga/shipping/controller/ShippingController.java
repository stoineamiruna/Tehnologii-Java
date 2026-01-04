package com.saga.shipping.controller;

import com.saga.shipping.dto.OrderDto;
import com.saga.shipping.dto.ServiceResponse;
import com.saga.shipping.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/ship")
    public ResponseEntity<ServiceResponse> shipOrder(@RequestBody OrderDto order) {
        ServiceResponse response = shippingService.shipOrder(order);
        return ResponseEntity.ok(response);
    }
}