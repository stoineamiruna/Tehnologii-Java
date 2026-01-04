package com.saga.order.service;

import com.saga.order.dto.OrderRequest;
import com.saga.order.dto.OrderResponse;
import com.saga.order.model.Order;
import com.saga.order.model.OrderStatus;
import com.saga.order.repository.OrderRepository;
import com.saga.order.saga.OrderSagaOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderSagaOrchestrator sagaOrchestrator;

    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for customer: {}", request.getCustomerId());

        // Create initial order
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Save order
        order = orderRepository.save(order);
        log.info("Order created with ID: {}", order.getId());

        // Execute saga
        order = sagaOrchestrator.executeOrderSaga(order);

        // Build response
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setCustomerId(order.getCustomerId());
        response.setProductId(order.getProductId());
        response.setQuantity(order.getQuantity());
        response.setAmount(order.getAmount());
        response.setStatus(order.getStatus());

        if (order.getStatus() == OrderStatus.COMPLETED) {
            response.setMessage("Order completed successfully");
        } else {
            response.setMessage("Order failed: " + order.getFailureReason());
        }

        return response;
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
}