package com.saga.shipping.service;

import com.saga.shipping.dto.OrderDto;
import com.saga.shipping.dto.ServiceResponse;
import com.saga.shipping.model.Shipment;
import com.saga.shipping.model.ShipmentStatus;
import com.saga.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final ShipmentRepository shipmentRepository;

    // PIVOT TRANSACTION: Ship order (point of no return)
    public ServiceResponse shipOrder(OrderDto order) {
        log.info("PIVOT TRANSACTION: Shipping order ID: {}", order.getId());
        log.warn("This is the point of no return - cannot be compensated!");

        try {
            // Check if already shipped (idempotency)
            if (shipmentRepository.findByOrderId(order.getId()).isPresent()) {
                log.warn("Order already shipped for Order ID: {}", order.getId());
                return new ServiceResponse(true, "Order already shipped", null);
            }

            // Create shipment - THIS CANNOT BE UNDONE
            Shipment shipment = new Shipment();
            shipment.setOrderId(order.getId());
            shipment.setCustomerId(order.getCustomerId());
            shipment.setTrackingNumber("TRACK-" + UUID.randomUUID().toString().substring(0, 8));
            shipment.setStatus(ShipmentStatus.SHIPPED);
            shipment.setCreatedAt(LocalDateTime.now());
            shipment.setUpdatedAt(LocalDateTime.now());

            shipmentRepository.save(shipment);
            log.info("Order shipped successfully with tracking: {}", shipment.getTrackingNumber());
            log.info("SAGA IS NOW COMMITTED - All remaining steps must succeed!");

            return new ServiceResponse(true, "Order shipped successfully", shipment);

        } catch (Exception e) {
            log.error("CRITICAL: Pivot transaction failed: {}", e.getMessage());
            return new ServiceResponse(false, "Failed to ship order: " + e.getMessage(), null);
        }
    }
}