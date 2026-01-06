package com.university.eventsourcing.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.university.eventsourcing.aggregate.BankAccount;
import com.university.eventsourcing.domain.Event;
import com.university.eventsourcing.domain.Snapshot;
import com.university.eventsourcing.events.*;
import com.university.eventsourcing.repository.EventRepository;
import com.university.eventsourcing.repository.SnapshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventStore {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private SnapshotRepository snapshotRepository;

    @Autowired
    private EventPublisher eventPublisher;

    private final ObjectMapper objectMapper;
    private static final int SNAPSHOT_FREQUENCY = 5; // Create snapshot every 5 events

    public EventStore() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Transactional
    public void saveEvents(String aggregateId, List<AccountEvent> events) {
        for (AccountEvent event : events) {
            Event eventEntity = new Event(
                    aggregateId,
                    event.getEventType(),
                    serializeEvent(event),
                    event.getTimestamp(),
                    event.getVersion()
            );
            eventRepository.save(eventEntity);

            // Publish event to subscribers
            eventPublisher.publish(event);
        }

        // Check if we need to create a snapshot
        long eventCount = eventRepository.findByAggregateIdOrderByVersionAsc(aggregateId).size();
        if (eventCount % SNAPSHOT_FREQUENCY == 0) {
            createSnapshot(aggregateId);
        }
    }

    public BankAccount loadAggregate(String aggregateId) {
        BankAccount account = new BankAccount();

        // Try to load the latest snapshot
        Optional<Snapshot> snapshotOpt = snapshotRepository.findTopByAggregateIdOrderByVersionDesc(aggregateId);

        if (snapshotOpt.isPresent()) {
            Snapshot snapshot = snapshotOpt.get();
            account = deserializeSnapshot(snapshot.getSnapshotData());

            // Load events after the snapshot
            List<Event> eventsAfterSnapshot = eventRepository
                    .findByAggregateIdAndVersionGreaterThanOrderByVersionAsc(aggregateId, snapshot.getVersion());

            for (Event event : eventsAfterSnapshot) {
                AccountEvent accountEvent = deserializeEvent(event.getEventData(), event.getEventType());
                account.apply(accountEvent);
            }
        } else {
            // No snapshot, load all events
            List<Event> events = eventRepository.findByAggregateIdOrderByVersionAsc(aggregateId);

            for (Event event : events) {
                AccountEvent accountEvent = deserializeEvent(event.getEventData(), event.getEventType());
                account.apply(accountEvent);
            }
        }

        return account;
    }

    private void createSnapshot(String aggregateId) {
        BankAccount account = loadAggregate(aggregateId);

        Snapshot snapshot = new Snapshot(
                aggregateId,
                serializeSnapshot(account),
                account.getVersion(),
                LocalDateTime.now()
        );

        snapshotRepository.save(snapshot);
        System.out.println("Snapshot created for account: " + aggregateId + " at version: " + account.getVersion());
    }

    private String serializeEvent(AccountEvent event) {
        try {
            // Create a wrapper that only includes the fields we want
            EventWrapper wrapper = new EventWrapper();
            wrapper.accountId = event.getAccountId();
            wrapper.timestamp = event.getTimestamp();
            wrapper.version = event.getVersion();

            if (event instanceof AccountCreatedEvent) {
                wrapper.ownerName = ((AccountCreatedEvent) event).getOwnerName();
            } else if (event instanceof MoneyDepositedEvent) {
                wrapper.amount = ((MoneyDepositedEvent) event).getAmount();
            } else if (event instanceof MoneyWithdrawnEvent) {
                wrapper.amount = ((MoneyWithdrawnEvent) event).getAmount();
            } else if (event instanceof MoneyTransferredEvent) {
                wrapper.amount = ((MoneyTransferredEvent) event).getAmount();
                wrapper.toAccountId = ((MoneyTransferredEvent) event).getToAccountId();
            }

            return objectMapper.writeValueAsString(wrapper);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }

    private String serializeSnapshot(BankAccount account) {
        try {
            return objectMapper.writeValueAsString(account);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize snapshot", e);
        }
    }

    private AccountEvent deserializeEvent(String eventData, String eventType) {
        try {
            EventWrapper wrapper = objectMapper.readValue(eventData, EventWrapper.class);

            return switch (eventType) {
                case "AccountCreated" -> new AccountCreatedEvent(
                        wrapper.accountId,
                        wrapper.ownerName,
                        wrapper.timestamp,
                        wrapper.version
                );
                case "MoneyDeposited" -> new MoneyDepositedEvent(
                        wrapper.accountId,
                        wrapper.amount,
                        wrapper.timestamp,
                        wrapper.version
                );
                case "MoneyWithdrawn" -> new MoneyWithdrawnEvent(
                        wrapper.accountId,
                        wrapper.amount,
                        wrapper.timestamp,
                        wrapper.version
                );
                case "MoneyTransferred" -> new MoneyTransferredEvent(
                        wrapper.accountId,
                        wrapper.amount,
                        wrapper.toAccountId,
                        wrapper.timestamp,
                        wrapper.version
                );
                default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
            };
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize event: " + e.getMessage(), e);
        }
    }

    private BankAccount deserializeSnapshot(String snapshotData) {
        try {
            return objectMapper.readValue(snapshotData, BankAccount.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize snapshot", e);
        }
    }

    public List<Event> getEventHistory(String aggregateId) {
        return eventRepository.findByAggregateIdOrderByVersionAsc(aggregateId);
    }

    // Inner class for serialization wrapper
    private static class EventWrapper {
        public String accountId;
        public LocalDateTime timestamp;
        public Long version;
        public String ownerName;
        public java.math.BigDecimal amount;
        public String toAccountId;
    }
}