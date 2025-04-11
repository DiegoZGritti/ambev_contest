package com.contest.ambev.adapters.output.persistence;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.ports.output.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    
    private final JpaOrderRepository jpaOrderRepository;
    
    @Override
    public Order save(Order order) {
        return jpaOrderRepository.save(order);
    }
    
    @Override
    public List<Order> findAll() {
        return jpaOrderRepository.findAll();
    }
    
    @Override
    public Optional<Order> findByExternalId(String externalId) {
        return jpaOrderRepository.findByExternalId(externalId);
    }
    
    @Override
    public boolean existsByExternalId(String externalId) {
        return jpaOrderRepository.existsByExternalId(externalId);
    }
    
    @Override
    public long count() {
        return jpaOrderRepository.count();
    }
} 
