package com.university.eventsourcing.controller;

import com.university.eventsourcing.aggregate.BankAccount;
import com.university.eventsourcing.domain.Event;
import com.university.eventsourcing.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class BankAccountController {

    @Autowired
    private BankAccountService bankAccountService;

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(@RequestBody Map<String, String> request) {
        String accountId = request.get("accountId");
        String ownerName = request.get("ownerName");
        bankAccountService.createAccount(accountId, ownerName);
        return ResponseEntity.ok("Account created successfully");
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<String> deposit(
            @PathVariable String accountId,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        bankAccountService.depositMoney(accountId, amount);
        return ResponseEntity.ok("Money deposited successfully");
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<String> withdraw(
            @PathVariable String accountId,
            @RequestBody Map<String, BigDecimal> request) {
        BigDecimal amount = request.get("amount");
        bankAccountService.withdrawMoney(accountId, amount);
        return ResponseEntity.ok("Money withdrawn successfully");
    }

    @PostMapping("/{accountId}/transfer")
    public ResponseEntity<String> transfer(
            @PathVariable String accountId,
            @RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String toAccountId = request.get("toAccountId").toString();
        bankAccountService.transferMoney(accountId, amount, toAccountId);
        return ResponseEntity.ok("Money transferred successfully");
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable String accountId) {
        BankAccount account = bankAccountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountId}/history")
    public ResponseEntity<List<Event>> getHistory(@PathVariable String accountId) {
        List<Event> history = bankAccountService.getAccountHistory(accountId);
        return ResponseEntity.ok(history);
    }
}