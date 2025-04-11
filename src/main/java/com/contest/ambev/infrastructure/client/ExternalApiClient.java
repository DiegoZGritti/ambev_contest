package com.contest.ambev.infrastructure.client;

import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${app.external-api.base-url:http://localhost:9090}")
    private String baseUrl;
    
    
    public Optional<Map<String, Object>> sendOrder(OrderRequestDTO orderRequestDTO) {
        String url = getBaseUrl() + "/pedidos";
        log.info("Enviando pedido {} para a API externa: {}", orderRequestDTO.getExternalId(), url);
        
        try {
            HttpEntity<OrderRequestDTO> request = new HttpEntity<>(orderRequestDTO);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Pedido {} enviado com sucesso. Resposta: {}", orderRequestDTO.getExternalId(), response.getBody());
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                return Optional.of(responseBody);
            } else {
                log.error("Falha ao enviar pedido {}. Status: {}", orderRequestDTO.getExternalId(), response.getStatusCode());
                return Optional.empty();
            }
        } catch (RestClientException e) {
            log.error("Erro ao enviar pedido {}: {}", orderRequestDTO.getExternalId(), e.getMessage());
            return Optional.empty();
        }
    }
    
    
    public Optional<Map<String, Object>> getOrderStatus(String orderId) {
        String url = getBaseUrl() + "/pedidos/" + orderId;
        log.info("Consultando status do pedido {} na API externa: {}", orderId, url);
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("Status do pedido {} obtido com sucesso. Resposta: {}", orderId, response.getBody());
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                return Optional.of(responseBody);
            } else {
                log.error("Falha ao consultar status do pedido {}. Status: {}", orderId, response.getStatusCode());
                return Optional.empty();
            }
        } catch (RestClientException e) {
            log.error("Erro ao consultar status do pedido {}: {}", orderId, e.getMessage());
            return Optional.empty();
        }
    }
    
    
    public String getBaseUrl() {
        return baseUrl;
    }
} 
