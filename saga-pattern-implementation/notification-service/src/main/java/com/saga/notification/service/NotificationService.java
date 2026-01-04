package com.saga.notification.service;

import com.saga.notification.dto.OrderDto;
import com.saga.notification.dto.ServiceResponse;
import com.saga.notification.model.Notification;
import com.saga.notification.model.NotificationStatus;
import com.saga.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final Random random = new Random();

    // RETRIABLE TRANSACTION: Send notification (will retry until success)
    public ServiceResponse sendNotification(OrderDto order) {
        log.info("RETRIABLE TRANSACTION: Sending notification for Order ID: {}", order.getId());

        try {
            // Check if notification already sent (idempotency)
            Notification existingNotification = notificationRepository
                    .findByOrderId(order.getId())
                    .orElse(null);

            if (existingNotification != null &&
                    existingNotification.getStatus() == NotificationStatus.SENT) {
                log.warn("Notification already sent for Order ID: {}", order.getId());
                return new ServiceResponse(true, "Notification already sent", existingNotification);
            }

            // Simulate occasional failures to demonstrate retry mechanism
            // 30% chance of failure on first attempts
            boolean shouldFail = random.nextInt(10) < 3;

            if (existingNotification == null) {
                existingNotification = new Notification();
                existingNotification.setOrderId(order.getId());
                existingNotification.setCustomerId(order.getCustomerId());
                existingNotification.setMessage("Your order #" + order.getId() + " has been completed successfully!");
                existingNotification.setStatus(NotificationStatus.PENDING);
                existingNotification.setAttemptCount(0);
                existingNotification.setCreatedAt(LocalDateTime.now());
            }

            existingNotification.setAttemptCount(existingNotification.getAttemptCount() + 1);

            if (shouldFail && existingNotification.getAttemptCount() < 2) {
                log.warn("Simulated notification failure (attempt {}). Will retry...",
                        existingNotification.getAttemptCount());
                existingNotification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(existingNotification);
                return new ServiceResponse(false, "Notification service temporarily unavailable", null);
            }

            // Send notification successfully
            existingNotification.setStatus(NotificationStatus.SENT);
            existingNotification.setSentAt(LocalDateTime.now());
            notificationRepository.save(existingNotification);

            log.info("Notification sent successfully for Order ID: {} (attempt {})",
                    order.getId(), existingNotification.getAttemptCount());

            return new ServiceResponse(true, "Notification sent successfully", existingNotification);

        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage());
            return new ServiceResponse(false, "Error sending notification: " + e.getMessage(), null);
        }
    }
}