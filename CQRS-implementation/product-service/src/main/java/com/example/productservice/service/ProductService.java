package com.example.productservice.service;

import com.example.productservice.dto.CreateProductRequest;
import com.example.productservice.entity.OutboxEvent;
import com.example.productservice.entity.Product;
import com.example.productservice.event.ProductEvent;
import com.example.productservice.repository.OutboxEventRepository;
import com.example.productservice.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Product createProduct(CreateProductRequest request) throws JsonProcessingException {
        // Step 1: Create and save the product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        Product savedProduct = productRepository.save(product);

        // Step 2: Create the event
        ProductEvent productEvent = new ProductEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getStock(),
                "PRODUCT_CREATED"
        );

        // Step 3: Insert event into Outbox table
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateId(String.valueOf(savedProduct.getId()));
        outboxEvent.setEventType("PRODUCT_CREATED");
        outboxEvent.setPayload(objectMapper.writeValueAsString(productEvent));
        outboxEvent.setCreatedAt(LocalDateTime.now());
        outboxEvent.setPublished(false);

        outboxEventRepository.save(outboxEvent);

        return savedProduct;
    }
}