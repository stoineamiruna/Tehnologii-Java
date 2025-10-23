package com.example.lab3.order.aspect;

import com.example.lab3.order.exception.IneligibleCustomerException;
import com.example.lab3.order.model.Customer;
import com.example.lab3.order.model.Order;
import com.example.lab3.order.repository.CustomerRepository;
import com.example.lab3.order.service.DiscountService;
import com.example.lab3.order.service.LoyaltyDiscountService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class DiscountAspect {

    private final CustomerRepository customerRepository;
    private final DiscountService discountService; // injectam strategia curenta

    public DiscountAspect(CustomerRepository customerRepository, DiscountService discountService) {
        this.customerRepository = customerRepository;
        this.discountService = discountService;
    }

    @Before("execution(* com.example.lab3.order.service.OrderService.applyDiscount(..)) && args(order)")
    public void checkCustomerAndEligibility(JoinPoint jp, Order order) {
        Optional<Customer> customerOpt = customerRepository.findById(order.getCustomerId());

        if (customerOpt.isEmpty()) {
            throw new IneligibleCustomerException("Customer not found: id=" + order.getCustomerId());
        }

        Customer customer = customerOpt.get();

        // Verificam eligibilitatea in functie de strategia activa
        if (discountService instanceof LoyaltyDiscountService && !customer.isLoyal()) {
            throw new IneligibleCustomerException(
                    "Customer not eligible for loyalty discount: " + customer.getName()
            );
        }
    }
}
