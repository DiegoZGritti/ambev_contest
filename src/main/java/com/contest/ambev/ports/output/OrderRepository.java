package com.contest.ambev.ports.output;

import com.contest.ambev.domain.entity.Order;

import java.util.List;
import java.util.Optional;


public interface OrderRepository {
    
    
    Order save(Order order);
    
    
    List<Order> findAll();
    
    
    Optional<Order> findByExternalId(String externalId);
    
    
    boolean existsByExternalId(String externalId);
    
    
    long count();
} 
