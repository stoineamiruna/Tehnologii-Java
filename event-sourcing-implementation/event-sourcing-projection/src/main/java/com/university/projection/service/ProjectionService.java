package com.university.projection.service;

import com.university.projection.model.AccountProjection;
import com.university.projection.repository.AccountProjectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectionService {

    @Autowired
    private AccountProjectionRepository projectionRepository;

    @Transactional
    public void handleAccountCreated(String accountId, String ownerName) {
        AccountProjection projection = new AccountProjection(accountId, ownerName);
        projectionRepository.save(projection);
        System.out.println("Projection created for account: " + accountId);
    }

    @Transactional
    public void handleMoneyDeposited(String accountId, BigDecimal amount) {
        AccountProjection projection = projectionRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        projection.setCurrentBalance(projection.getCurrentBalance().add(amount));
        projection.setTotalDeposited(projection.getTotalDeposited().add(amount));
        projection.setTotalTransactions(projection.getTotalTransactions() + 1);
        projection.setLastUpdated(LocalDateTime.now());

        projectionRepository.save(projection);
        System.out.println("Projection updated: Money deposited for account " + accountId);
    }

    @Transactional
    public void handleMoneyWithdrawn(String accountId, BigDecimal amount) {
        AccountProjection projection = projectionRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        projection.setCurrentBalance(projection.getCurrentBalance().subtract(amount));
        projection.setTotalWithdrawn(projection.getTotalWithdrawn().add(amount));
        projection.setTotalTransactions(projection.getTotalTransactions() + 1);
        projection.setLastUpdated(LocalDateTime.now());

        projectionRepository.save(projection);
        System.out.println("Projection updated: Money withdrawn for account " + accountId);
    }

    @Transactional
    public void handleMoneyTransferred(String accountId, BigDecimal amount) {
        AccountProjection projection = projectionRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        projection.setCurrentBalance(projection.getCurrentBalance().subtract(amount));
        projection.setTotalWithdrawn(projection.getTotalWithdrawn().add(amount));
        projection.setTotalTransactions(projection.getTotalTransactions() + 1);
        projection.setLastUpdated(LocalDateTime.now());

        projectionRepository.save(projection);
        System.out.println("Projection updated: Money transferred from account " + accountId);
    }

    public AccountProjection getProjection(String accountId) {
        return projectionRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account projection not found"));
    }

    public List<AccountProjection> getAllProjections() {
        return projectionRepository.findAll();
    }
}