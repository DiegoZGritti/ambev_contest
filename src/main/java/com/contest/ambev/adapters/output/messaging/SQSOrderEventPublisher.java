package com.contest.ambev.adapters.output.messaging;

import com.amazonaws.services.sqs.AmazonSQS;
import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.ports.output.OrderEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.features.sqs-enabled", havingValue = "true", matchIfMissing = false)
public class SQSOrderEventPublisher implements OrderEventPublisher {
    
    private final AmazonSQS amazonSQS;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.sqs.queue.order-received}")
    private String orderReceivedQueue;
    
    @Value("${aws.sqs.queue.order-processed}")
    private String orderProcessedQueue;
    
    @Override
    public void publishOrderReceived(OrderRequestDTO orderRequest) {
        log.info("Publicando evento de pedido recebido no SQS: {}", orderRequest.getExternalId());
        try {
            String message = objectMapper.writeValueAsString(orderRequest);
            amazonSQS.sendMessage(orderReceivedQueue, message);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar pedido recebido: {}", e.getMessage());
            throw new RuntimeException("Erro ao publicar evento de pedido recebido", e);
        }
    }
    
    @Override
    public void publishOrderProcessed(Order order) {
        log.info("Publicando evento de pedido processado no SQS: {}", order.getExternalId());
        try {
            String message = objectMapper.writeValueAsString(order);
            amazonSQS.sendMessage(orderProcessedQueue, message);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar pedido processado: {}", e.getMessage());
            throw new RuntimeException("Erro ao publicar evento de pedido processado", e);
        }
    }
} 
