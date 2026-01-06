package com.example.orderhistoryservice.cache;

import com.example.orderhistoryservice.event.ProductEvent;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductCache {

    private final Map<Long, ProductEvent> products = new ConcurrentHashMap<>();

    public void put(Long productId, ProductEvent productEvent) {
        products.put(productId, productEvent);
    }

    public ProductEvent get(Long productId) {
        return products.get(productId);
    }
}