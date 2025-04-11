package com.contest.ambev.adapters.input.messaging;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.ports.input.OrderUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMessageConsumer {
    
    private final AmazonSQS amazonSQS;
    private final OrderUseCase orderUseCase;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.sqs.queue.order-received}")
    private String orderReceivedQueue;
    
    
    @Scheduled(fixedDelay = 5000) 
    public void consumeOrderReceivedMessages() {
        try {
            String queueUrl = amazonSQS.getQueueUrl(orderReceivedQueue).getQueueUrl();
            ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
                    .withQueueUrl(queueUrl)
                    .withMaxNumberOfMessages(10);
            
            List<Message> messages = amazonSQS.receiveMessage(receiveRequest).getMessages();
            
            for (Message message : messages) {
                try {
                    log.info("Recebida mensagem de pedido do SQS: {}", message.getBody());
                    OrderRequestDTO orderRequest = objectMapper.readValue(message.getBody(), OrderRequestDTO.class);
                    orderUseCase.createOrder(orderRequest);
                    log.info("Pedido processado com sucesso: {}", orderRequest.getExternalId());
                    
                    
                    amazonSQS.deleteMessage(queueUrl, message.getReceiptHandle());
                } catch (Exception e) {
                    log.error("Erro ao processar mensagem SQS: {}", e.getMessage());
                    
                }
            }
        } catch (Exception e) {
            log.error("Erro ao acessar fila SQS: {}", e.getMessage());
        }
    }
} 
