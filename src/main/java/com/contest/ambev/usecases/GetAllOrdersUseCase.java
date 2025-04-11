package com.contest.ambev.usecases;

import com.contest.ambev.interfaces.dto.OrderResponseDTO;

import java.util.List;


public interface GetAllOrdersUseCase {
    
    
    List<OrderResponseDTO> execute();
} 
