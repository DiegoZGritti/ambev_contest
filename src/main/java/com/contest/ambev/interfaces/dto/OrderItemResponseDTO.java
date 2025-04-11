package com.contest.ambev.interfaces.dto;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class OrderItemResponseDTO {
    private Long id;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
} 
