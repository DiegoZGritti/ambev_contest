package com.contest.ambev.adapters.outbound;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.ports.output.ServiceBPort;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceBClient implements ServiceBPort {

    private final RestTemplate restTemplate;
    private final WireMockServer wireMockServer;

    @Value("${service-b.url:http://localhost:9090/service-b/orders}")
    private String serviceBDefaultUrl;

    @Override
    public boolean sendOrderToServiceB(Order order) {
        try {
            
            String serviceBUrl = wireMockServer != null && wireMockServer.isRunning() 
                ? "http://localhost:" + wireMockServer.port() + "/service-b/orders"
                : serviceBDefaultUrl;
                
            log.info("Enviando pedido {} para o Serviço B em {}", order.getExternalId(), serviceBUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("orderId", order.getExternalId());
            orderMap.put("totalValue", order.getTotalValue());
            orderMap.put("items", order.getItems());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(orderMap, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    serviceBUrl, 
                    request, 
                    Map.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Pedido {} enviado com sucesso para o Serviço B. Resposta: {}", 
                        order.getExternalId(), response.getBody());
                return true;
            } else {
                log.error("Falha ao enviar pedido {} para o Serviço B. Status: {}", 
                        order.getExternalId(), response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Erro ao enviar pedido {} para o Serviço B: {}", 
                    order.getExternalId(), e.getMessage(), e);
            return false;
        }
    }
} 
