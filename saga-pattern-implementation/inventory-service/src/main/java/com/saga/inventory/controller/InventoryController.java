package com.saga.inventory.controller;

import com.saga.inventory.dto.OrderDto;
import com.saga.inventory.dto.ServiceResponse;
import com.saga.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<ServiceResponse> reserveInventory(@RequestBody OrderDto order) {
        ServiceResponse response = inventoryService.reserveInventory(order);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/release")
    public ResponseEntity<ServiceResponse> releaseInventory(@RequestBody OrderDto order) {
        ServiceResponse response = inventoryService.releaseInventory(order);
        return ResponseEntity.ok(response);
    }
}