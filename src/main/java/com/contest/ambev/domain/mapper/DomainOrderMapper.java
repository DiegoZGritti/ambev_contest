package com.contest.ambev.domain.mapper;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import com.contest.ambev.interfaces.dto.OrderItemRequestDTO;
import com.contest.ambev.interfaces.dto.OrderItemResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface DomainOrderMapper {
    
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalValue", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Order toEntity(OrderRequestDTO dto);
    
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "description", target = "productName")
    @Mapping(source = "unitPrice", target = "price")
    OrderItem toEntity(OrderItemRequestDTO dto);
    
    
    OrderResponseDTO toResponseDTO(Order order);
    
    
    OrderItemResponseDTO toResponseDTO(OrderItem item);
} 
