package com.contest.ambev.application.port;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;


public interface OrderMapperPort {
    
    
    Order toEntity(OrderRequestDTO dto);
    
    
    OrderResponseDTO toResponseDTO(Order order);
} 
