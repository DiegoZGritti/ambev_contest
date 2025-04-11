package com.contest.ambev.adapters.input.messaging;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderStatus;
import com.contest.ambev.domain.mapper.DomainOrderMapper;
import com.contest.ambev.domain.service.OrderCalculationService;
import com.contest.ambev.interfaces.dto.OrderItemRequestDTO;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.ports.output.OrderEventPublisher;
import com.contest.ambev.ports.output.OrderRepository;
import com.contest.ambev.ports.output.ServiceBPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderProcessingConsumerTest {

    @Mock
    private AmazonSQS amazonSQS;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DomainOrderMapper orderMapper;

    @Mock
    private OrderCalculationService orderCalculationService;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private ServiceBPort serviceBPort;

    @InjectMocks
    private OrderProcessingConsumer orderProcessingConsumer;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    private OrderRequestDTO orderRequestDTO;
    private Order order;
    private Message sqsMessage;
    private String queueUrl;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        
        ReflectionTestUtils.setField(orderProcessingConsumer, "createOrderQueue", "test-queue");
        
        queueUrl = "https://sqs.us-east-1.amazonaws.com/123456789012/test-queue";
        when(amazonSQS.getQueueUrl("test-queue")).thenReturn(new GetQueueUrlResult().withQueueUrl(queueUrl));

        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("PROD001");
        itemDTO.setDescription("Test Product");
        itemDTO.setQuantity(10);
        itemDTO.setUnitPrice(new BigDecimal("9.99"));

        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setExternalId("ORDER-SQS-123");
        orderRequestDTO.setItems(Collections.singletonList(itemDTO));

        order = new Order();
        order.setExternalId("ORDER-SQS-123");
        order.setStatus(OrderStatus.RECEBIDO);
        
        sqsMessage = new Message()
                .withBody("{\"externalId\":\"ORDER-SQS-123\"}")
                .withReceiptHandle("receipt-handle-123");
    }

    @Test
    void processMessages_NoMessages_ShouldReturnEarly() {
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(new ReceiveMessageResult());

        orderProcessingConsumer.processMessages();

        verify(amazonSQS).getQueueUrl("test-queue");
        verify(amazonSQS).receiveMessage(any(ReceiveMessageRequest.class));
        verifyNoMoreInteractions(orderRepository, orderMapper, orderCalculationService);
    }

    @Test
    void processMessages_WithMessages_ShouldProcessEach() throws JsonProcessingException {
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(
                new ReceiveMessageResult().withMessages(Collections.singletonList(sqsMessage)));
        
        when(objectMapper.readValue(sqsMessage.getBody(), OrderRequestDTO.class)).thenReturn(orderRequestDTO);
        when(orderRepository.existsByExternalId(orderRequestDTO.getExternalId())).thenReturn(false);
        when(orderMapper.toEntity(orderRequestDTO)).thenReturn(order);
        when(orderCalculationService.calculateTotalValue(order)).thenReturn(new BigDecimal("99.90"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(serviceBPort.sendOrderToServiceB(any(Order.class))).thenReturn(true);

        orderProcessingConsumer.processMessages();

        verify(amazonSQS).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(anyString(), eq(OrderRequestDTO.class));
        verify(orderRepository).existsByExternalId(orderRequestDTO.getExternalId());
        verify(orderMapper).toEntity(orderRequestDTO);
        verify(orderCalculationService).calculateTotalValue(order);
        verify(orderRepository, times(3)).save(any(Order.class)); 
        verify(serviceBPort).sendOrderToServiceB(any(Order.class));
        verify(orderEventPublisher).publishOrderProcessed(any(Order.class));
        verify(amazonSQS).deleteMessage(queueUrl, "receipt-handle-123");
    }

    @Test
    void processMessages_DuplicateOrder_ShouldSkipProcessing() throws JsonProcessingException {
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(
                new ReceiveMessageResult().withMessages(Collections.singletonList(sqsMessage)));
        
        when(objectMapper.readValue(sqsMessage.getBody(), OrderRequestDTO.class)).thenReturn(orderRequestDTO);
        when(orderRepository.existsByExternalId(orderRequestDTO.getExternalId())).thenReturn(true);

        orderProcessingConsumer.processMessages();

        verify(amazonSQS).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(anyString(), eq(OrderRequestDTO.class));
        verify(orderRepository).existsByExternalId(orderRequestDTO.getExternalId());
        verify(amazonSQS).deleteMessage(queueUrl, "receipt-handle-123");
        
        verifyNoMoreInteractions(orderMapper, orderCalculationService, serviceBPort);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void processMessages_ServiceBFails_ShouldSetErrorStatus() throws JsonProcessingException {
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(
                new ReceiveMessageResult().withMessages(Collections.singletonList(sqsMessage)));
        
        when(objectMapper.readValue(sqsMessage.getBody(), OrderRequestDTO.class)).thenReturn(orderRequestDTO);
        when(orderRepository.existsByExternalId(orderRequestDTO.getExternalId())).thenReturn(false);
        when(orderMapper.toEntity(orderRequestDTO)).thenReturn(order);
        when(orderCalculationService.calculateTotalValue(order)).thenReturn(new BigDecimal("99.90"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(serviceBPort.sendOrderToServiceB(any(Order.class))).thenReturn(false);

        orderProcessingConsumer.processMessages();

        verify(amazonSQS).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(anyString(), eq(OrderRequestDTO.class));
        verify(orderRepository).existsByExternalId(orderRequestDTO.getExternalId());
        verify(orderMapper).toEntity(orderRequestDTO);
        verify(orderCalculationService).calculateTotalValue(order);
        verify(orderRepository, times(3)).save(orderCaptor.capture());
        verify(serviceBPort).sendOrderToServiceB(any(Order.class));
        verify(amazonSQS).deleteMessage(queueUrl, "receipt-handle-123");

        assertEquals(OrderStatus.ERRO, orderCaptor.getAllValues().get(2).getStatus());
    }

    @Test
    void processMessages_JsonParsingException_ShouldCatchAndContinue() throws JsonProcessingException {
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(
                new ReceiveMessageResult().withMessages(Collections.singletonList(sqsMessage)));
        
        when(objectMapper.readValue(anyString(), eq(OrderRequestDTO.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        orderProcessingConsumer.processMessages();

        verify(amazonSQS).receiveMessage(any(ReceiveMessageRequest.class));
        verify(objectMapper).readValue(anyString(), eq(OrderRequestDTO.class));
        
        verify(amazonSQS, never()).deleteMessage(anyString(), anyString());
    }
} 
