package com.example.orderservice.service;

import com.example.orderservice.dto.CreateOrderRequest;
import com.example.orderservice.entity.Order;
import com.example.orderservice.entity.OutboxEvent;
import com.example.orderservice.event.OrderEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(CreateOrderRequest request) throws JsonProcessingException {
        // Step 1: Create and save the order (business data)
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setTotalPrice(request.getTotalPrice());
        order.setStatus("CREATED");
        order.setOrderDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Step 2: Create the event
        OrderEvent orderEvent = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getProductId(),
                savedOrder.getQuantity(),
                savedOrder.getTotalPrice(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                "ORDER_CREATED"
        );

        // Step 3: Insert event into Outbox table (same transaction)
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateId(String.valueOf(savedOrder.getId()));
        outboxEvent.setEventType("ORDER_CREATED");
        outboxEvent.setPayload(objectMapper.writeValueAsString(orderEvent));
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setPublished(false);

        outboxEventRepository.save(outboxEvent);

        // Both operations are in the same transaction - GUARANTEED CONSISTENCY
        return savedOrder;
    }
}