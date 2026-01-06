package com.university.eventsourcing.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AccountCreatedEvent extends AccountEvent {
    private String ownerName;

    public AccountCreatedEvent(String accountId, String ownerName, LocalDateTime timestamp, Long version) {
        super(accountId, timestamp, version);
        this.ownerName = ownerName;
    }

    @Override
    public String getEventType() {
        return "AccountCreated";
    }
}