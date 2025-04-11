package com.contest.ambev.interfaces.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequestDTO {
    @NotBlank(message = "O ID externo do pedido é obrigatório")
    private String externalId;
    
    @NotEmpty(message = "A lista de itens não pode ser vazia")
    @Valid
    private List<OrderItemRequestDTO> items;
} 
