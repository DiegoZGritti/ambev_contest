package com.contest.ambev.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    private Order order;
    private OrderItem orderItem;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        
        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProductId("PROD-001");
        orderItem.setProductName("Test Product");
        orderItem.setQuantity(5);
        orderItem.setPrice(new BigDecimal("10.00"));
        
        order = new Order();
        order.setId(1L);
        order.setExternalId("ORDER-TEST-001");
        order.setStatus(OrderStatus.RECEBIDO);
        order.setCreatedAt(now);
        order.setTotalValue(new BigDecimal("50.00"));
        order.setItems(Collections.singletonList(orderItem));
    }

    @Test
    void testOrderBasicProperties() {
        assertEquals(1L, order.getId());
        assertEquals("ORDER-TEST-001", order.getExternalId());
        assertEquals(OrderStatus.RECEBIDO, order.getStatus());
        assertEquals(now, order.getCreatedAt());
        assertEquals(new BigDecimal("50.00"), order.getTotalValue());
        assertNotNull(order.getItems());
        assertEquals(1, order.getItems().size());
    }

    @Test
    void testOrderItemBasicProperties() {
        OrderItem item = order.getItems().get(0);
        assertEquals(1L, item.getId());
        assertEquals("PROD-001", item.getProductId());
        assertEquals("Test Product", item.getProductName());
        assertEquals(5, item.getQuantity());
        assertEquals(new BigDecimal("10.00"), item.getPrice());
    }

    @Test
    void testOrderStatusTransitions() {
        
        assertEquals(OrderStatus.RECEBIDO, order.getStatus());
        
        
        order.setStatus(OrderStatus.PROCESSANDO);
        assertEquals(OrderStatus.PROCESSANDO, order.getStatus());
        
        order.setStatus(OrderStatus.ENVIADO);
        assertEquals(OrderStatus.ENVIADO, order.getStatus());
        
        
        Order errorOrder = new Order();
        errorOrder.setStatus(OrderStatus.RECEBIDO);
        errorOrder.setStatus(OrderStatus.PROCESSANDO);
        errorOrder.setStatus(OrderStatus.ERRO);
        assertEquals(OrderStatus.ERRO, errorOrder.getStatus());
    }

    @Test
    void testAddOrderItem() {
        
        Order newOrder = new Order();
        newOrder.setItems(new ArrayList<>());
        
        OrderItem item1 = new OrderItem();
        item1.setProductId("PROD-001");
        
        OrderItem item2 = new OrderItem();
        item2.setProductId("PROD-002");
        
        
        newOrder.getItems().add(item1);
        newOrder.getItems().add(item2);
        
        
        assertEquals(2, newOrder.getItems().size());
        assertEquals("PROD-001", newOrder.getItems().get(0).getProductId());
        assertEquals("PROD-002", newOrder.getItems().get(1).getProductId());
    }
    
    @Test
    void testOrderToString() {
        
        String orderString = order.toString();
        
        
        assertNotNull(orderString);
        assertTrue(orderString.length() > 0);
    }
} 
