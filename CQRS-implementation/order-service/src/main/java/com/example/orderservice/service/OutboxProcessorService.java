package com.example.orderservice.service;

import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessorService {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String ORDER_TOPIC = "order-events";

    /**
     * This is the Message Relay / Outbox Processor
     * It polls the outbox table every 5 seconds and publishes unpublished events
     */
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> unpublishedEvents = outboxEventRepository
                .findByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : unpublishedEvents) {
            try {
                // Publish event to Kafka - payload is already JSON string
                kafkaTemplate.send(ORDER_TOPIC, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Published event: {} for aggregate: {}",
                                        event.getEventType(), event.getAggregateId());
                            } else {
                                log.error("Failed to publish event: {}", event.getId(), ex);
                            }
                        });

                // Mark as published
                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
                log.error("Failed to publish event: {}", event.getId(), e);
                // Event remains unpublished and will be retried in next poll
            }
        }
    }
}