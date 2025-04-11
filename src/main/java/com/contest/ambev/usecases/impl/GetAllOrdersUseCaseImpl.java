package com.contest.ambev.usecases.impl;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.mapper.DomainOrderMapper;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import com.contest.ambev.ports.output.OrderRepository;
import com.contest.ambev.usecases.GetAllOrdersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class GetAllOrdersUseCaseImpl implements GetAllOrdersUseCase {
    
    private final OrderRepository orderRepository;
    private final DomainOrderMapper orderMapper;
    
    
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> execute() {
        log.info("Buscando todos os pedidos");
        
        List<Order> orders = orderRepository.findAll();
        log.info("Encontrados {} pedidos", orders.size());
        
        return orders.stream()
                .map(orderMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
} 
