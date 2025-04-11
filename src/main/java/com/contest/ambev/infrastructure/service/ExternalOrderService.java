package com.contest.ambev.infrastructure.service;

import com.contest.ambev.infrastructure.client.ExternalApiClient;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalOrderService {

    private final ExternalApiClient externalApiClient;
    
    
    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Optional<String> sendOrderToExternalSystem(OrderRequestDTO orderRequestDTO) {
        log.info("Tentando enviar pedido {} para sistema externo", orderRequestDTO.getExternalId());
        
        Optional<Map<String, Object>> response = externalApiClient.sendOrder(orderRequestDTO);
        
        return response.map(responseMap -> {
            String confirmationCode = (String) responseMap.get("confirmationCode");
            log.info("Pedido {} confirmado com código {}", orderRequestDTO.getExternalId(), confirmationCode);
            return confirmationCode;
        });
    }
    
    
    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 2,
        backoff = @Backoff(delay = 500)
    )
    public Optional<String> getOrderStatusFromExternalSystem(String orderId) {
        log.info("Consultando status do pedido {} no sistema externo", orderId);
        
        Optional<Map<String, Object>> response = externalApiClient.getOrderStatus(orderId);
        
        return response.map(responseMap -> {
            String status = (String) responseMap.get("status");
            log.info("Status do pedido {}: {}", orderId, status);
            return status;
        });
    }
} 
