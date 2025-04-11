package com.contest.ambev.application.service;

import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import com.contest.ambev.ports.input.OrderUseCase;
import com.contest.ambev.usecases.CreateOrderUseCase;
import com.contest.ambev.usecases.GetAllOrdersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderUseCase {
    
    private final CreateOrderUseCase createOrderUseCase;
    private final GetAllOrdersUseCase getAllOrdersUseCase;
    
    @Override
    public OrderResponseDTO createOrder(OrderRequestDTO requestDTO) {
        log.info("Iniciando criação de pedido pelo serviço: {}", requestDTO.getExternalId());
        return createOrderUseCase.execute(requestDTO);
    }
    
    @Override
    public List<OrderResponseDTO> getAllOrders() {
        log.info("Buscando todos os pedidos pelo serviço");
        return getAllOrdersUseCase.execute();
    }
} 
