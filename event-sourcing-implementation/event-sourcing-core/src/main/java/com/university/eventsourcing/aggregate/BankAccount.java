package com.university.eventsourcing.aggregate;

import com.university.eventsourcing.events.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class BankAccount {
    private String accountId;
    private String ownerName;
    private BigDecimal balance;
    private Long version;
    private List<AccountEvent> uncommittedEvents;

    public BankAccount() {
        this.balance = BigDecimal.ZERO;
        this.version = 0L;
        this.uncommittedEvents = new ArrayList<>();
    }

    // Apply event (used when replaying from event store)
    public void apply(AccountEvent event) {
        if (event instanceof AccountCreatedEvent) {
            applyAccountCreated((AccountCreatedEvent) event);
        } else if (event instanceof MoneyDepositedEvent) {
            applyMoneyDeposited((MoneyDepositedEvent) event);
        } else if (event instanceof MoneyWithdrawnEvent) {
            applyMoneyWithdrawn((MoneyWithdrawnEvent) event);
        } else if (event instanceof MoneyTransferredEvent) {
            applyMoneyTransferred((MoneyTransferredEvent) event);
        }
        this.version = event.getVersion();
    }

    private void applyAccountCreated(AccountCreatedEvent event) {
        this.accountId = event.getAccountId();
        this.ownerName = event.getOwnerName();
        this.balance = BigDecimal.ZERO;
    }

    private void applyMoneyDeposited(MoneyDepositedEvent event) {
        this.balance = this.balance.add(event.getAmount());
    }

    private void applyMoneyWithdrawn(MoneyWithdrawnEvent event) {
        this.balance = this.balance.subtract(event.getAmount());
    }

    private void applyMoneyTransferred(MoneyTransferredEvent event) {
        this.balance = this.balance.subtract(event.getAmount());
    }

    // Commands that generate events
    public void createAccount(String accountId, String ownerName) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                accountId,
                ownerName,
                java.time.LocalDateTime.now(),
                this.version + 1
        );
        applyAndRecord(event);
    }

    public void depositMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        MoneyDepositedEvent event = new MoneyDepositedEvent(
                this.accountId,
                amount,
                java.time.LocalDateTime.now(),
                this.version + 1
        );
        applyAndRecord(event);
    }

    public void withdrawMoney(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        MoneyWithdrawnEvent event = new MoneyWithdrawnEvent(
                this.accountId,
                amount,
                java.time.LocalDateTime.now(),
                this.version + 1
        );
        applyAndRecord(event);
    }

    public void transferMoney(BigDecimal amount, String toAccountId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        MoneyTransferredEvent event = new MoneyTransferredEvent(
                this.accountId,
                amount,
                toAccountId,
                java.time.LocalDateTime.now(),
                this.version + 1
        );
        applyAndRecord(event);
    }

    private void applyAndRecord(AccountEvent event) {
        apply(event);
        uncommittedEvents.add(event);
    }

    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
}