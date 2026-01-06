package com.example.productservice.service;

import com.example.productservice.entity.OutboxEvent;
import com.example.productservice.repository.OutboxEventRepository;
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

    private static final String PRODUCT_TOPIC = "product-events";

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> unpublishedEvents = outboxEventRepository
                .findByPublishedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : unpublishedEvents) {
            try {
                kafkaTemplate.send(PRODUCT_TOPIC, event.getAggregateId(), event.getPayload())
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Published event: {} for aggregate: {}",
                                        event.getEventType(), event.getAggregateId());
                            } else {
                                log.error("Failed to publish event: {}", event.getId(), ex);
                            }
                        });

                event.setPublished(true);
                event.setPublishedAt(LocalDateTime.now());
                outboxEventRepository.save(event);

            } catch (Exception e) {
                log.error("Failed to publish event: {}", event.getId(), e);
            }
        }
    }
}