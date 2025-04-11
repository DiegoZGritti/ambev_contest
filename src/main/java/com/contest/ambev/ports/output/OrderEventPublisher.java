package com.contest.ambev.ports.output;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;


public interface OrderEventPublisher {
    
    
    void publishOrderReceived(OrderRequestDTO orderRequest);
    
    
    void publishOrderProcessed(Order order);
} 
