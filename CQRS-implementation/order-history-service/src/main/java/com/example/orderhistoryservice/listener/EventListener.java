package com.example.orderhistoryservice.listener;

import com.example.orderhistoryservice.cache.ProductCache;
import com.example.orderhistoryservice.cache.UserCache;
import com.example.orderhistoryservice.document.OrderHistory;
import com.example.orderhistoryservice.event.OrderEvent;
import com.example.orderhistoryservice.event.ProductEvent;
import com.example.orderhistoryservice.event.UserEvent;
import com.example.orderhistoryservice.repository.OrderHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final OrderHistoryRepository orderHistoryRepository;
    private final UserCache userCache;
    private final ProductCache productCache;
    private final ObjectMapper objectMapper;

    /**
     * Listen to ORDER events from Kafka and update the read model (MongoDB)
     */
    @KafkaListener(topics = "order-events", groupId = "order-history-group")
    public void handleOrderEvent(String message) {
        try {
            OrderEvent orderEvent = objectMapper.readValue(message, OrderEvent.class);
            log.info("Received Order Event: {}", orderEvent);

            // Get user and product info from cache
            UserEvent userEvent = userCache.get(orderEvent.getUserId());
            ProductEvent productEvent = productCache.get(orderEvent.getProductId());

            // Create or update the denormalized read model in MongoDB
            OrderHistory orderHistory = new OrderHistory();
            orderHistory.setOrderId(orderEvent.getOrderId());
            orderHistory.setUserId(orderEvent.getUserId());
            orderHistory.setProductId(orderEvent.getProductId());
            orderHistory.setQuantity(orderEvent.getQuantity());
            orderHistory.setTotalPrice(orderEvent.getTotalPrice());
            orderHistory.setStatus(orderEvent.getStatus());
            orderHistory.setOrderDate(orderEvent.getOrderDate());
            orderHistory.setCreatedAt(LocalDateTime.now());

            // Add denormalized user data
            if (userEvent != null) {
                orderHistory.setUserName(userEvent.getName());
                orderHistory.setUserEmail(userEvent.getEmail());
            }

            // Add denormalized product data
            if (productEvent != null) {
                orderHistory.setProductName(productEvent.getName());
                orderHistory.setProductPrice(productEvent.getPrice());
            }

            orderHistoryRepository.save(orderHistory);
            log.info("Saved Order History to MongoDB: {}", orderHistory);

        } catch (Exception e) {
            log.error("Error processing order event", e);
        }
    }

    /**
     * Listen to USER events and cache user data
     */
    @KafkaListener(topics = "user-events", groupId = "order-history-group")
    public void handleUserEvent(String message) {
        try {
            UserEvent userEvent = objectMapper.readValue(message, UserEvent.class);
            log.info("Received User Event: {}", userEvent);

            // Cache user data for future order events
            userCache.put(userEvent.getUserId(), userEvent);

        } catch (Exception e) {
            log.error("Error processing user event", e);
        }
    }

    /**
     * Listen to PRODUCT events and cache product data
     */
    @KafkaListener(topics = "product-events", groupId = "order-history-group")
    public void handleProductEvent(String message) {
        try {
            ProductEvent productEvent = objectMapper.readValue(message, ProductEvent.class);
            log.info("Received Product Event: {}", productEvent);

            // Cache product data for future order events
            productCache.put(productEvent.getProductId(), productEvent);

        } catch (Exception e) {
            log.error("Error processing product event", e);
        }
    }
}