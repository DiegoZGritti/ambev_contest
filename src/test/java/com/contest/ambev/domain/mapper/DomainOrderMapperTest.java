package com.contest.ambev.domain.mapper;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;
import com.contest.ambev.domain.entity.OrderStatus;
import com.contest.ambev.interfaces.dto.OrderItemRequestDTO;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class DomainOrderMapperTest {

    private DomainOrderMapper mapper;
    private OrderRequestDTO orderRequestDTO;
    private Order order;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(DomainOrderMapper.class);
        
        
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("PROD-001");
        itemDTO.setDescription("Test Product");
        itemDTO.setQuantity(5);
        itemDTO.setUnitPrice(new BigDecimal("10.00"));
        
        orderRequestDTO = new OrderRequestDTO();
        orderRequestDTO.setExternalId("ORDER-TEST-001");
        orderRequestDTO.setItems(Collections.singletonList(itemDTO));
        
        
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId("PROD-001");
        item.setProductName("Test Product");
        item.setQuantity(5);
        item.setPrice(new BigDecimal("10.00"));
        
        order = new Order();
        order.setId(1L);
        order.setExternalId("ORDER-TEST-001");
        order.setStatus(OrderStatus.ENVIADO);
        order.setTotalValue(new BigDecimal("50.00"));
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(Collections.singletonList(item));
    }

    @Test
    void toEntity_ShouldMapRequestDTOToEntity() {
        
        Order result = mapper.toEntity(orderRequestDTO);
        
        
        assertNotNull(result);
        assertEquals(orderRequestDTO.getExternalId(), result.getExternalId());
        
        
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals(orderRequestDTO.getItems().get(0).getProductId(), result.getItems().get(0).getProductId());
        assertEquals(orderRequestDTO.getItems().get(0).getDescription(), result.getItems().get(0).getProductName());
        assertEquals(orderRequestDTO.getItems().get(0).getQuantity(), result.getItems().get(0).getQuantity());
        assertEquals(orderRequestDTO.getItems().get(0).getUnitPrice(), result.getItems().get(0).getPrice());
    }

    @Test
    void toResponseDTO_ShouldMapEntityToResponseDTO() {
        
        OrderResponseDTO result = mapper.toResponseDTO(order);
        
        
        assertNotNull(result);
        assertEquals(order.getId(), result.getId());
        assertEquals(order.getExternalId(), result.getExternalId());
        assertEquals(order.getStatus(), result.getStatus());
        assertEquals(order.getTotalValue(), result.getTotalValue());
        assertEquals(order.getCreatedAt(), result.getCreatedAt());
        
        
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals(order.getItems().get(0).getId(), result.getItems().get(0).getId());
        assertEquals(order.getItems().get(0).getProductId(), result.getItems().get(0).getProductId());
        assertEquals(order.getItems().get(0).getProductName(), result.getItems().get(0).getProductName());
        assertEquals(order.getItems().get(0).getQuantity(), result.getItems().get(0).getQuantity());
        assertEquals(order.getItems().get(0).getPrice(), result.getItems().get(0).getPrice());
    }
} 
