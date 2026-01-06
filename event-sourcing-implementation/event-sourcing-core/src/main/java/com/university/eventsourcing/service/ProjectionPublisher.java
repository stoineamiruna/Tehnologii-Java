package com.university.eventsourcing.service;

import com.university.eventsourcing.events.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProjectionPublisher {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PROJECTION_SERVICE_URL = "http://localhost:8082/api/projections/events";

    @Autowired
    private EventPublisher eventPublisher;

    public void init() {
        eventPublisher.subscribe(this::publishToProjectionService);
    }

    private void publishToProjectionService(AccountEvent event) {
        try {
            if (event instanceof AccountCreatedEvent) {
                publishAccountCreated((AccountCreatedEvent) event);
            } else if (event instanceof MoneyDepositedEvent) {
                publishMoneyDeposited((MoneyDepositedEvent) event);
            } else if (event instanceof MoneyWithdrawnEvent) {
                publishMoneyWithdrawn((MoneyWithdrawnEvent) event);
            } else if (event instanceof MoneyTransferredEvent) {
                publishMoneyTransferred((MoneyTransferredEvent) event);
            }
        } catch (Exception e) {
            System.err.println("Failed to publish event to projection service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void publishAccountCreated(AccountCreatedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("accountId", event.getAccountId());
        payload.put("ownerName", event.getOwnerName());
        System.out.println("Publishing AccountCreated to projection service: " + event.getAccountId());
        restTemplate.postForObject(PROJECTION_SERVICE_URL + "/account-created", payload, String.class);
    }

    private void publishMoneyDeposited(MoneyDepositedEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("accountId", event.getAccountId());
        payload.put("amount", event.getAmount().toString());
        System.out.println("Publishing MoneyDeposited to projection service: " + event.getAccountId() + ", amount: " + event.getAmount());
        restTemplate.postForObject(PROJECTION_SERVICE_URL + "/money-deposited", payload, String.class);
    }

    private void publishMoneyWithdrawn(MoneyWithdrawnEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("accountId", event.getAccountId());
        payload.put("amount", event.getAmount().toString());
        System.out.println("Publishing MoneyWithdrawn to projection service: " + event.getAccountId() + ", amount: " + event.getAmount());
        restTemplate.postForObject(PROJECTION_SERVICE_URL + "/money-withdrawn", payload, String.class);
    }

    private void publishMoneyTransferred(MoneyTransferredEvent event) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("accountId", event.getAccountId());
        payload.put("amount", event.getAmount().toString());
        payload.put("toAccountId", event.getToAccountId());
        System.out.println("Publishing MoneyTransferred to projection service: " + event.getAccountId() + ", amount: " + event.getAmount());
        restTemplate.postForObject(PROJECTION_SERVICE_URL + "/money-transferred", payload, String.class);
    }
}