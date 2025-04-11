package com.contest.ambev.usecases;

import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;


public interface CreateOrderUseCase {
    
    
    OrderResponseDTO execute(OrderRequestDTO requestDTO);
} 
