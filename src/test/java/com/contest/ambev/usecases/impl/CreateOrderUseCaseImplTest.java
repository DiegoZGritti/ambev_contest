package com.contest.ambev.usecases.impl;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderStatus;
import com.contest.ambev.domain.mapper.DomainOrderMapper;
import com.contest.ambev.domain.service.OrderCalculationService;
import com.contest.ambev.interfaces.dto.OrderItemRequestDTO;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import com.contest.ambev.ports.output.OrderEventPublisher;
import com.contest.ambev.ports.output.OrderRepository;
import com.contest.ambev.ports.output.ServiceBPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateOrderUseCaseImplTest {

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
    private CreateOrderUseCaseImpl createOrderUseCase;

    private OrderRequestDTO requestDTO;
    private Order order;
    private OrderResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        
        ReflectionTestUtils.setField(createOrderUseCase, "sqsEnabled", false);

        
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("PROD001");
        itemDTO.setDescription("Test Product");
        itemDTO.setQuantity(10);
        itemDTO.setUnitPrice(new BigDecimal("9.99"));

        requestDTO = new OrderRequestDTO();
        requestDTO.setExternalId("ORDER-123");
        requestDTO.setItems(Collections.singletonList(itemDTO));

        
        order = new Order();
        order.setId(1L);
        order.setExternalId("ORDER-123");
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.RECEBIDO);
        order.setTotalValue(new BigDecimal("99.90"));

        
        responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setExternalId("ORDER-123");
        responseDTO.setStatus(OrderStatus.ENVIADO);
        responseDTO.setTotalValue(new BigDecimal("99.90"));
    }

    @Test
    void execute_WhenSqsDisabled_ShouldProcessOrderDirectly() {
        
        when(orderRepository.existsByExternalId(requestDTO.getExternalId())).thenReturn(false);
        when(orderMapper.toEntity(requestDTO)).thenReturn(order);
        when(orderCalculationService.calculateTotalValue(order)).thenReturn(new BigDecimal("99.90"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(serviceBPort.sendOrderToServiceB(any(Order.class))).thenReturn(true);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(responseDTO);

        
        OrderResponseDTO result = createOrderUseCase.execute(requestDTO);

        
        assertNotNull(result);
        assertEquals("ORDER-123", result.getExternalId());
        assertEquals(OrderStatus.ENVIADO, result.getStatus());
        assertEquals(new BigDecimal("99.90"), result.getTotalValue());

        
        verify(orderRepository).existsByExternalId(requestDTO.getExternalId());
        verify(orderMapper).toEntity(requestDTO);
        verify(orderCalculationService).calculateTotalValue(order);
        verify(orderRepository, times(3)).save(any(Order.class)); 
        verify(serviceBPort).sendOrderToServiceB(any(Order.class));
        verify(orderEventPublisher).publishOrderReceived(requestDTO);
        verify(orderEventPublisher).publishOrderProcessed(any(Order.class));
    }

    @Test
    void execute_WhenSqsEnabled_ShouldPublishToQueue() {
        
        ReflectionTestUtils.setField(createOrderUseCase, "sqsEnabled", true);
        when(orderRepository.existsByExternalId(requestDTO.getExternalId())).thenReturn(false);
        when(orderMapper.toEntity(requestDTO)).thenReturn(order);
        when(orderCalculationService.calculateTotalValue(order)).thenReturn(new BigDecimal("99.90"));
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(responseDTO);

        
        OrderResponseDTO result = createOrderUseCase.execute(requestDTO);

        
        assertNotNull(result);
        assertEquals("ORDER-123", result.getExternalId());

        
        verify(orderRepository).existsByExternalId(requestDTO.getExternalId());
        verify(orderEventPublisher).publishOrderReceived(requestDTO);
        verify(orderMapper).toEntity(requestDTO);
        verify(orderMapper).toResponseDTO(any(Order.class));
        
        
        verify(orderRepository, never()).save(any(Order.class));
        verify(serviceBPort, never()).sendOrderToServiceB(any(Order.class));
    }

    @Test
    void execute_WhenOrderExists_ShouldThrowException() {
        
        when(orderRepository.existsByExternalId(requestDTO.getExternalId())).thenReturn(true);

        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            createOrderUseCase.execute(requestDTO);
        });

        assertTrue(exception.getMessage().contains("já existe"));
        verify(orderRepository).existsByExternalId(requestDTO.getExternalId());
        verify(orderMapper, never()).toEntity(any(OrderRequestDTO.class));
    }

    @Test
    void execute_WhenServiceBFails_ShouldSetStatusToError() {
        
        when(orderRepository.existsByExternalId(requestDTO.getExternalId())).thenReturn(false);
        when(orderMapper.toEntity(requestDTO)).thenReturn(order);
        when(orderCalculationService.calculateTotalValue(order)).thenReturn(new BigDecimal("99.90"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(serviceBPort.sendOrderToServiceB(any(Order.class))).thenReturn(false);
        
        
        responseDTO.setStatus(OrderStatus.ERRO);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(responseDTO);

        
        OrderResponseDTO result = createOrderUseCase.execute(requestDTO);

        
        assertNotNull(result);
        assertEquals(OrderStatus.ERRO, result.getStatus());
        
        
        verify(orderRepository, times(3)).save(any(Order.class)); 
    }

    @Test
    void execute_WhenServiceBThrowsException_ShouldCatchAndSetStatusToError() {
        
        when(orderRepository.existsByExternalId(requestDTO.getExternalId())).thenReturn(false);
        when(orderMapper.toEntity(requestDTO)).thenReturn(order);
        when(orderCalculationService.calculateTotalValue(order)).thenReturn(new BigDecimal("99.90"));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(serviceBPort.sendOrderToServiceB(any(Order.class))).thenThrow(new RuntimeException("Service B error"));
        
        
        responseDTO.setStatus(OrderStatus.ERRO);
        when(orderMapper.toResponseDTO(any(Order.class))).thenReturn(responseDTO);

        
        OrderResponseDTO result = createOrderUseCase.execute(requestDTO);

        
        assertNotNull(result);
        assertEquals(OrderStatus.ERRO, result.getStatus());
        
        
        verify(orderRepository, times(3)).save(any(Order.class)); 
    }
} 
