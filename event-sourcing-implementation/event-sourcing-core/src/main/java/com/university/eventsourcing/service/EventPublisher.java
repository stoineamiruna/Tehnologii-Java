package com.university.eventsourcing.service;

import com.university.eventsourcing.events.AccountEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service
public class EventPublisher {

    private final List<Consumer<AccountEvent>> subscribers = new ArrayList<>();

    public void subscribe(Consumer<AccountEvent> subscriber) {
        subscribers.add(subscriber);
    }

    @Async
    public void publish(AccountEvent event) {
        System.out.println("Publishing event: " + event.getEventType() + " for account: " + event.getAccountId());
        for (Consumer<AccountEvent> subscriber : subscribers) {
            subscriber.accept(event);
        }
    }
}