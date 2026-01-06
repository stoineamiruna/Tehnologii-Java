package com.university.eventsourcing.service;

import com.university.eventsourcing.aggregate.BankAccount;
import com.university.eventsourcing.domain.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BankAccountService {

    @Autowired
    private EventStore eventStore;

    public void createAccount(String accountId, String ownerName) {
        BankAccount account = new BankAccount();
        account.createAccount(accountId, ownerName);
        eventStore.saveEvents(accountId, account.getUncommittedEvents());
        account.markEventsAsCommitted();
    }

    public void depositMoney(String accountId, BigDecimal amount) {
        BankAccount account = eventStore.loadAggregate(accountId);
        account.depositMoney(amount);
        eventStore.saveEvents(accountId, account.getUncommittedEvents());
        account.markEventsAsCommitted();
    }

    public void withdrawMoney(String accountId, BigDecimal amount) {
        BankAccount account = eventStore.loadAggregate(accountId);
        account.withdrawMoney(amount);
        eventStore.saveEvents(accountId, account.getUncommittedEvents());
        account.markEventsAsCommitted();
    }

    public void transferMoney(String fromAccountId, BigDecimal amount, String toAccountId) {
        BankAccount account = eventStore.loadAggregate(fromAccountId);
        account.transferMoney(amount, toAccountId);
        eventStore.saveEvents(fromAccountId, account.getUncommittedEvents());
        account.markEventsAsCommitted();
    }

    public BankAccount getAccount(String accountId) {
        return eventStore.loadAggregate(accountId);
    }

    public List<Event> getAccountHistory(String accountId) {
        return eventStore.getEventHistory(accountId);
    }
}