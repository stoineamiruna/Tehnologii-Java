package com.university.projection.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_projection")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountProjection {

    @Id
    private String accountId;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private BigDecimal currentBalance;

    @Column(nullable = false)
    private Integer totalTransactions;

    @Column(nullable = false)
    private BigDecimal totalDeposited;

    @Column(nullable = false)
    private BigDecimal totalWithdrawn;

    @Column
    private LocalDateTime lastUpdated;

    public AccountProjection(String accountId, String ownerName) {
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.currentBalance = BigDecimal.ZERO;
        this.totalTransactions = 0;
        this.totalDeposited = BigDecimal.ZERO;
        this.totalWithdrawn = BigDecimal.ZERO;
        this.lastUpdated = LocalDateTime.now();
    }
}