package com.university.eventsourcing.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MoneyTransferredEvent extends AccountEvent {
    private BigDecimal amount;
    private String toAccountId;

    public MoneyTransferredEvent(String accountId, BigDecimal amount, String toAccountId, LocalDateTime timestamp, Long version) {
        super(accountId, timestamp, version);
        this.amount = amount;
        this.toAccountId = toAccountId;
    }

    @Override
    public String getEventType() {
        return "MoneyTransferred";
    }
}