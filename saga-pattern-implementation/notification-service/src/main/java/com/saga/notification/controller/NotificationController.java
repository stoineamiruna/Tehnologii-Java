package com.saga.notification.controller;

import com.saga.notification.dto.OrderDto;
import com.saga.notification.dto.ServiceResponse;
import com.saga.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ServiceResponse> sendNotification(@RequestBody OrderDto order) {
        ServiceResponse response = notificationService.sendNotification(order);
        return ResponseEntity.ok(response);
    }
}