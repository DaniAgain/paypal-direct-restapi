package com.example.paypaldirectrestapi.repositories;


import com.example.paypaldirectrestapi.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
}
