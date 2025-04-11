package com.contest.ambev.adapters.output.mapper;

import com.contest.ambev.application.port.OrderMapperPort;
import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.mapper.DomainOrderMapper;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OrderMapperAdapter implements OrderMapperPort {

    private final DomainOrderMapper orderMapper;

    @Override
    public Order toEntity(OrderRequestDTO dto) {
        return orderMapper.toEntity(dto);
    }

    @Override
    public OrderResponseDTO toResponseDTO(Order order) {
        return orderMapper.toResponseDTO(order);
    }
} 
