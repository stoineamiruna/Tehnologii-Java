package com.saga.order.saga;

import com.saga.order.dto.ServiceResponse;
import com.saga.order.model.Order;
import com.saga.order.model.OrderStatus;
import com.saga.order.model.SagaStep;
import com.saga.order.model.SagaStepType;
import com.saga.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSagaOrchestrator {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${shipping.service.url}")
    private String shippingServiceUrl;

    @Value("${notification.service.url}")
    private String notificationServiceUrl;

    @Value("${saga.max.retry.attempts}")
    private int maxRetryAttempts;

    @Value("${saga.retry.delay.ms}")
    private long retryDelayMs;

    // Define saga steps with their types
    private final Map<SagaStep, SagaStepType> sagaSteps = new LinkedHashMap<>() {{
        put(SagaStep.RESERVE_PAYMENT, SagaStepType.COMPENSATABLE);
        put(SagaStep.RESERVE_INVENTORY, SagaStepType.COMPENSATABLE);
        put(SagaStep.SHIP_ORDER, SagaStepType.PIVOT);
        put(SagaStep.SEND_NOTIFICATION, SagaStepType.RETRIABLE);
    }};

    public Order executeOrderSaga(Order order) {
        log.info("Starting Saga for Order ID: {}", order.getId());

        List<SagaStep> completedSteps = new ArrayList<>();

        try {
            // Execute all saga steps
            for (Map.Entry<SagaStep, SagaStepType> entry : sagaSteps.entrySet()) {
                SagaStep step = entry.getKey();
                SagaStepType stepType = entry.getValue();

                log.info("Executing step: {} (Type: {})", step, stepType);

                boolean success = executeStep(step, order, stepType);

                if (success) {
                    completedSteps.add(step);
                    updateOrderStatus(order, step);
                } else {
                    log.error("Step {} failed. Initiating compensation...", step);
                    compensate(completedSteps, order);
                    order.setStatus(OrderStatus.FAILED);
                    order.setFailureReason("Failed at step: " + step);
                    return orderRepository.save(order);
                }
            }

            // All steps completed successfully
            order.setStatus(OrderStatus.COMPLETED);
            order.setUpdatedAt(LocalDateTime.now());
            log.info("Saga completed successfully for Order ID: {}", order.getId());
            return orderRepository.save(order);

        } catch (Exception e) {
            log.error("Unexpected error during saga execution", e);
            compensate(completedSteps, order);
            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason("Unexpected error: " + e.getMessage());
            return orderRepository.save(order);
        }
    }

    private boolean executeStep(SagaStep step, Order order, SagaStepType stepType) {
        switch (stepType) {
            case COMPENSATABLE:
            case PIVOT:
                return executeOnce(step, order);
            case RETRIABLE:
                return executeWithRetry(step, order);
            default:
                return false;
        }
    }

    private boolean executeOnce(SagaStep step, Order order) {
        try {
            String url = getServiceUrl(step, order);
            log.info("Calling service: {}", url);

            ServiceResponse response = restTemplate.postForObject(url, order, ServiceResponse.class);

            if (response != null && response.isSuccess()) {
                log.info("Step {} succeeded", step);
                return true;
            } else {
                log.error("Step {} failed: {}", step, response != null ? response.getMessage() : "No response");
                return false;
            }
        } catch (Exception e) {
            log.error("Error executing step {}: {}", step, e.getMessage());
            return false;
        }
    }

    private boolean executeWithRetry(SagaStep step, Order order) {
        int attempts = 0;

        while (attempts < maxRetryAttempts) {
            attempts++;
            log.info("Attempt {}/{} for retriable step: {}", attempts, maxRetryAttempts, step);

            try {
                String url = getServiceUrl(step, order);
                ServiceResponse response = restTemplate.postForObject(url, order, ServiceResponse.class);

                if (response != null && response.isSuccess()) {
                    log.info("Retriable step {} succeeded on attempt {}", step, attempts);
                    return true;
                }

                log.warn("Retriable step {} failed on attempt {}. Retrying...", step, attempts);

            } catch (Exception e) {
                log.error("Error on attempt {} for step {}: {}", attempts, step, e.getMessage());
            }

            if (attempts < maxRetryAttempts) {
                try {
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }

        log.error("Retriable step {} failed after {} attempts", step, maxRetryAttempts);
        return true; // Retriable steps after pivot should eventually succeed, but we return true to continue
    }

    private void compensate(List<SagaStep> completedSteps, Order order) {
        log.info("Starting compensation for {} completed steps", completedSteps.size());

        // Compensate in reverse order
        Collections.reverse(completedSteps);

        for (SagaStep step : completedSteps) {
            SagaStepType stepType = sagaSteps.get(step);

            // Only compensate COMPENSATABLE steps
            if (stepType == SagaStepType.COMPENSATABLE) {
                log.info("Compensating step: {}", step);
                compensateStep(step, order);
            } else {
                log.info("Step {} is not compensatable (Type: {}), skipping compensation", step, stepType);
            }
        }

        log.info("Compensation completed");
    }

    private void compensateStep(SagaStep step, Order order) {
        try {
            String url = getCompensationUrl(step, order);
            log.info("Calling compensation service: {}", url);

            ServiceResponse response = restTemplate.postForObject(url, order, ServiceResponse.class);

            if (response != null && response.isSuccess()) {
                log.info("Compensation for step {} succeeded", step);
            } else {
                log.error("Compensation for step {} failed: {}", step,
                        response != null ? response.getMessage() : "No response");
            }
        } catch (Exception e) {
            log.error("Error during compensation of step {}: {}", step, e.getMessage());
        }
    }

    private String getServiceUrl(SagaStep step, Order order) {
        return switch (step) {
            case CREATE_ORDER -> ""; // Already created
            case RESERVE_PAYMENT -> paymentServiceUrl + "/payment/reserve";
            case RESERVE_INVENTORY -> inventoryServiceUrl + "/inventory/reserve";
            case SHIP_ORDER -> shippingServiceUrl + "/shipping/ship";
            case SEND_NOTIFICATION -> notificationServiceUrl + "/notification/send";
        };
    }

    private String getCompensationUrl(SagaStep step, Order order) {
        return switch (step) {
            case CREATE_ORDER -> ""; // Cancel is handled in service
            case RESERVE_PAYMENT -> paymentServiceUrl + "/payment/refund";
            case RESERVE_INVENTORY -> inventoryServiceUrl + "/inventory/release";
            case SHIP_ORDER -> ""; // Pivot - cannot compensate
            case SEND_NOTIFICATION -> ""; // Retriable - no compensation needed
        };
    }

    private void updateOrderStatus(Order order, SagaStep step) {
        OrderStatus newStatus = switch (step) {
            case CREATE_ORDER -> OrderStatus.PENDING;
            case RESERVE_PAYMENT -> OrderStatus.PAYMENT_RESERVED;
            case RESERVE_INVENTORY -> OrderStatus.INVENTORY_RESERVED;
            case SHIP_ORDER -> OrderStatus.SHIPPED;
            case SEND_NOTIFICATION -> OrderStatus.COMPLETED;
        };

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }
}
/*
-succes case test
{
    "customerId": "CUST001",
    "productId": "PROD123",
    "quantity": 5,
    "amount": 500.00
}

-error case test
 */