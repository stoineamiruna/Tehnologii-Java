package com.saga.inventory.service;

import com.saga.inventory.dto.OrderDto;
import com.saga.inventory.dto.ServiceResponse;
import com.saga.inventory.model.InventoryReservation;
import com.saga.inventory.model.ReservationStatus;
import com.saga.inventory.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryReservationRepository reservationRepository;

    // COMPENSATABLE TRANSACTION: Reserve inventory
    public ServiceResponse reserveInventory(OrderDto order) {
        log.info("Reserving inventory for Order ID: {}", order.getId());

        try {
            // Simulate inventory check
            if (order.getQuantity() > 100) {
                log.error("Insufficient inventory for quantity: {}", order.getQuantity());
                return new ServiceResponse(false, "Insufficient inventory", null);
            }

            // Check if already reserved (idempotency)
            if (reservationRepository.findByOrderId(order.getId()).isPresent()) {
                log.warn("Inventory already reserved for Order ID: {}", order.getId());
                return new ServiceResponse(true, "Inventory already reserved", null);
            }

            // Reserve inventory
            InventoryReservation reservation = new InventoryReservation();
            reservation.setOrderId(order.getId());
            reservation.setProductId(order.getProductId());
            reservation.setQuantity(order.getQuantity());
            reservation.setStatus(ReservationStatus.RESERVED);
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());

            reservationRepository.save(reservation);
            log.info("Inventory reserved successfully for Order ID: {}", order.getId());

            return new ServiceResponse(true, "Inventory reserved successfully", reservation);

        } catch (Exception e) {
            log.error("Error reserving inventory: {}", e.getMessage());
            return new ServiceResponse(false, "Error reserving inventory: " + e.getMessage(), null);
        }
    }

    // COMPENSATION: Release inventory
    public ServiceResponse releaseInventory(OrderDto order) {
        log.info("Releasing inventory for Order ID: {}", order.getId());

        try {
            InventoryReservation reservation = reservationRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new RuntimeException("Reservation not found"));

            reservation.setStatus(ReservationStatus.RELEASED);
            reservation.setUpdatedAt(LocalDateTime.now());
            reservationRepository.save(reservation);

            log.info("Inventory released successfully for Order ID: {}", order.getId());
            return new ServiceResponse(true, "Inventory released successfully", reservation);

        } catch (Exception e) {
            log.error("Error releasing inventory: {}", e.getMessage());
            return new ServiceResponse(false, "Error releasing inventory: " + e.getMessage(), null);
        }
    }
}