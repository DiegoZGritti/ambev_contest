package com.contest.ambev.ports.output;

import com.contest.ambev.domain.entity.Order;


public interface ServiceBPort {
    
    
    boolean sendOrderToServiceB(Order order);
} 
