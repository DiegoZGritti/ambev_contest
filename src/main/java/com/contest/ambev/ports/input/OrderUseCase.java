package com.contest.ambev.ports.input;

import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;

import java.util.List;


public interface OrderUseCase {
    
    
    OrderResponseDTO createOrder(OrderRequestDTO requestDTO);
    
    
    List<OrderResponseDTO> getAllOrders();
} 
