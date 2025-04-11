package com.contest.ambev.interfaces.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequestDTO {
    @NotBlank(message = "O ID do produto é obrigatório")
    private String productId;
    
    @NotBlank(message = "A descrição do produto é obrigatória")
    private String description;
    
    @NotNull(message = "A quantidade é obrigatória")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer quantity;
    
    @NotNull(message = "O preço unitário é obrigatório")
    @Positive(message = "O preço unitário deve ser maior que zero")
    private BigDecimal unitPrice;
} 
