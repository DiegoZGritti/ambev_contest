package com.contest.ambev.adapters.input.messaging;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderStatus;
import com.contest.ambev.domain.mapper.DomainOrderMapper;
import com.contest.ambev.domain.service.OrderCalculationService;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.ports.output.OrderEventPublisher;
import com.contest.ambev.ports.output.OrderRepository;
import com.contest.ambev.ports.output.ServiceBPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.features.sqs-enabled", havingValue = "true", matchIfMissing = false)
public class OrderProcessingConsumer {

    private final AmazonSQS amazonSQS;
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final DomainOrderMapper orderMapper;
    private final OrderCalculationService orderCalculationService;
    private final OrderEventPublisher orderEventPublisher;
    private final ServiceBPort serviceBPort;

    @Value("${aws.sqs.create-order-queue}")
    private String createOrderQueue;

    @Scheduled(fixedDelay = 5000) 
    @Transactional
    public void processMessages() {
        try {
            String queueUrl = amazonSQS.getQueueUrl(createOrderQueue).getQueueUrl();
            
            
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMaxNumberOfMessages(10)
                    .withWaitTimeSeconds(5);
            
            List<Message> messages = amazonSQS.receiveMessage(receiveRequest).getMessages();
            
            if (messages.isEmpty()) {
                return;
            }
            
            log.info("Recebidas {} mensagens para processamento", messages.size());
            
            for (Message message : messages) {
                try {
                    processMessage(message, queueUrl);
                } catch (Exception e) {
                    log.error("Erro ao processar mensagem: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao receber mensagens: {}", e.getMessage(), e);
        }
    }
    
    private void processMessage(Message message, String queueUrl) {
        try {
            
            OrderRequestDTO requestDTO = objectMapper.readValue(message.getBody(), OrderRequestDTO.class);
            
            log.info("Processando pedido com ID externo: {}", requestDTO.getExternalId());
            
            
            if (orderRepository.existsByExternalId(requestDTO.getExternalId())) {
                log.warn("Pedido já existe, ignorando: {}", requestDTO.getExternalId());
                
                amazonSQS.deleteMessage(queueUrl, message.getReceiptHandle());
                return;
            }
            
            
            Order order = orderMapper.toEntity(requestDTO);
            
            
            order.setStatus(OrderStatus.RECEBIDO);
            
            
            order.setTotalValue(orderCalculationService.calculateTotalValue(order));
            
            
            Order savedOrder = orderRepository.save(order);
            log.info("Pedido salvo no banco com status RECEBIDO: {}", savedOrder.getExternalId());
            
            try {
                
                savedOrder.setStatus(OrderStatus.PROCESSANDO);
                savedOrder = orderRepository.save(savedOrder);
                log.info("Status do pedido atualizado para PROCESSANDO: {}", savedOrder.getExternalId());
            
                
                log.info("Enviando pedido para o Serviço B: {}", savedOrder.getExternalId());
                boolean sentToServiceB = serviceBPort.sendOrderToServiceB(savedOrder);
                
                if (sentToServiceB) {
                    
                    savedOrder.setStatus(OrderStatus.ENVIADO);
                    savedOrder = orderRepository.save(savedOrder);
                    log.info("Pedido enviado com sucesso para o Serviço B. Status atualizado para ENVIADO: {}", 
                            savedOrder.getExternalId());
                    
                    
                    orderEventPublisher.publishOrderProcessed(savedOrder);
                } else {
                    
                    savedOrder.setStatus(OrderStatus.ERRO);
                    savedOrder = orderRepository.save(savedOrder);
                    log.error("Falha ao enviar pedido para o Serviço B. Status atualizado para ERRO: {}", 
                            savedOrder.getExternalId());
                }
            } catch (Exception e) {
                
                savedOrder.setStatus(OrderStatus.ERRO);
                savedOrder = orderRepository.save(savedOrder);
                log.error("Erro ao processar pedido: {}. Mensagem: {}", savedOrder.getExternalId(), e.getMessage(), e);
            }
            
            
            amazonSQS.deleteMessage(queueUrl, message.getReceiptHandle());
            
        } catch (Exception e) {
            log.error("Erro ao processar mensagem: {}", e.getMessage(), e);
            
        }
    }
} 
