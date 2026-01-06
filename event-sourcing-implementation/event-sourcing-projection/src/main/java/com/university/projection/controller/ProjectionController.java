package com.university.projection.controller;

import com.university.projection.model.AccountProjection;
import com.university.projection.service.ProjectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projections")
public class ProjectionController {

    @Autowired
    private ProjectionService projectionService;

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountProjection> getProjection(@PathVariable String accountId) {
        AccountProjection projection = projectionService.getProjection(accountId);
        return ResponseEntity.ok(projection);
    }

    @GetMapping
    public ResponseEntity<List<AccountProjection>> getAllProjections() {
        List<AccountProjection> projections = projectionService.getAllProjections();
        return ResponseEntity.ok(projections);
    }

    // Webhook endpoints to receive events from core service
    @PostMapping("/events/account-created")
    public ResponseEntity<String> handleAccountCreated(@RequestBody Map<String, Object> event) {
        try {
            String accountId = (String) event.get("accountId");
            String ownerName = (String) event.get("ownerName");

            if (accountId == null || ownerName == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            projectionService.handleAccountCreated(accountId, ownerName);
            System.out.println("Processed AccountCreated event for: " + accountId);
            return ResponseEntity.ok("Event processed");
        } catch (Exception e) {
            System.err.println("Error processing AccountCreated: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/events/money-deposited")
    public ResponseEntity<String> handleMoneyDeposited(@RequestBody Map<String, Object> event) {
        try {
            String accountId = (String) event.get("accountId");
            Object amountObj = event.get("amount");

            if (accountId == null || amountObj == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            BigDecimal amount = new BigDecimal(amountObj.toString());
            projectionService.handleMoneyDeposited(accountId, amount);
            System.out.println("Processed MoneyDeposited event for: " + accountId + ", amount: " + amount);
            return ResponseEntity.ok("Event processed");
        } catch (Exception e) {
            System.err.println("Error processing MoneyDeposited: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/events/money-withdrawn")
    public ResponseEntity<String> handleMoneyWithdrawn(@RequestBody Map<String, Object> event) {
        try {
            String accountId = (String) event.get("accountId");
            Object amountObj = event.get("amount");

            if (accountId == null || amountObj == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            BigDecimal amount = new BigDecimal(amountObj.toString());
            projectionService.handleMoneyWithdrawn(accountId, amount);
            System.out.println("Processed MoneyWithdrawn event for: " + accountId + ", amount: " + amount);
            return ResponseEntity.ok("Event processed");
        } catch (Exception e) {
            System.err.println("Error processing MoneyWithdrawn: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/events/money-transferred")
    public ResponseEntity<String> handleMoneyTransferred(@RequestBody Map<String, Object> event) {
        try {
            String accountId = (String) event.get("accountId");
            Object amountObj = event.get("amount");
            String toAccountId = (String) event.get("toAccountId");

            if (accountId == null || amountObj == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            BigDecimal amount = new BigDecimal(amountObj.toString());
            projectionService.handleMoneyTransferred(accountId, amount);
            System.out.println("Processed MoneyTransferred event for: " + accountId + ", amount: " + amount + ", to: " + toAccountId);
            return ResponseEntity.ok("Event processed");
        } catch (Exception e) {
            System.err.println("Error processing MoneyTransferred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}