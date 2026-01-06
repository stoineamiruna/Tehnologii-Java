package com.university.eventsourcing.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MoneyDepositedEvent extends AccountEvent {
    private BigDecimal amount;

    public MoneyDepositedEvent(String accountId, BigDecimal amount, LocalDateTime timestamp, Long version) {
        super(accountId, timestamp, version);
        this.amount = amount;
    }

    @Override
    public String getEventType() {
        return "MoneyDeposited";
    }
}