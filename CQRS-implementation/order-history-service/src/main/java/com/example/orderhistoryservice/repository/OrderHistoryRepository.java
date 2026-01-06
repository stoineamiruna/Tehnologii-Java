package com.example.orderhistoryservice.repository;

import com.example.orderhistoryservice.document.OrderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends MongoRepository<OrderHistory, String> {
    Optional<OrderHistory> findByOrderId(Long orderId);
    List<OrderHistory> findByUserId(Long userId);
}