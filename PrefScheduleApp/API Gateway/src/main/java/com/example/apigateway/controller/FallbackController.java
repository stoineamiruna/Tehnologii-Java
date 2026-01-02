package com.example.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/stablematch")
    public ResponseEntity<Map<String, Object>> stableMatchFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "StableMatch service is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "stablematch");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/prefschedule")
    public ResponseEntity<Map<String, Object>> prefScheduleFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SERVICE_UNAVAILABLE");
        response.put("message", "PrefSchedule service is currently unavailable. Please try again later.");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "prefschedule");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}