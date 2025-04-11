package com.contest.ambev.interfaces.dto;

import com.contest.ambev.domain.entity.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class OrderResponseDTO {
    private Long id;
    private String externalId;
    private List<OrderItemResponseDTO> items;
    private OrderStatus status;
    private BigDecimal totalValue;
    private LocalDateTime createdAt;
} 
