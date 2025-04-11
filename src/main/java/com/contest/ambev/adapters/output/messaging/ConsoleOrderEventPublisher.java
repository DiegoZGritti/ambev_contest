package com.contest.ambev.adapters.output.messaging;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.ports.output.OrderEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@ConditionalOnProperty(name = "app.features.sqs-enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleOrderEventPublisher implements OrderEventPublisher {

    @Override
    public void publishOrderReceived(OrderRequestDTO orderRequest) {
        log.info("SQS desabilitado - Evento de pedido recebido simulado para o ID externo: {}", 
                orderRequest.getExternalId());
    }

    @Override
    public void publishOrderProcessed(Order order) {
        log.info("SQS desabilitado - Evento de pedido processado simulado para o ID externo: {}", 
                order.getExternalId());
    }
} 
