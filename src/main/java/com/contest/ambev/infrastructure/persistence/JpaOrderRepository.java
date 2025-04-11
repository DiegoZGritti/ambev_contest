package com.contest.ambev.infrastructure.persistence;

import com.contest.ambev.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {
    
    
    Optional<Order> findByExternalId(String externalId);
    
    
    boolean existsByExternalId(String externalId);
} 
