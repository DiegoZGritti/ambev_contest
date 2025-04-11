package com.contest.ambev.controllers;

import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import com.contest.ambev.usecases.CreateOrderUseCase;
import com.contest.ambev.usecases.GetAllOrdersUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pedidos", description = "API para gerenciamento de pedidos")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetAllOrdersUseCase getAllOrdersUseCase;

    @PostMapping
    @Operation(summary = "Recebe pedidos com id externo para evitar duplicação")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        log.info("Recebendo pedido com ID externo: {}", request.getExternalId());
        
        
        if (request.getExternalId() == null || request.getExternalId().isEmpty()) {
            log.error("Pedido rejeitado: ID externo não fornecido");
            return ResponseEntity.badRequest().build();
        }
        
        OrderResponseDTO response = createOrderUseCase.execute(request);
        log.info("Pedido processado com sucesso: {}", response.getExternalId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os pedidos processados")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        log.info("Consultando todos os pedidos processados");
        List<OrderResponseDTO> orders = getAllOrdersUseCase.execute();
        log.info("Retornando {} pedidos processados", orders.size());
        return ResponseEntity.ok(orders);
    }
} 
