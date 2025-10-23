package com.example.lab3.order.repository;
import com.example.lab3.order.model.Customer;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class CustomerRepository {
    private final Map<Long, Customer> storage = new HashMap<>();

    @PostConstruct
    public void init() {
        // date de exemplu
        storage.put(1L, new Customer(1L, "Acme Corp", true));
        storage.put(2L, new Customer(2L, "Beta LLC", false));
        storage.put(3L, new Customer(3L, "Gamma S.A.", true));
    }

    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }
}
