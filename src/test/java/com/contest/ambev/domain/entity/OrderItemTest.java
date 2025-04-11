package com.contest.ambev.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class OrderItemTest {

    @Test
    void gettersAndSetters_WorkCorrectly() {
        
        OrderItem item = new OrderItem();
        
        
        item.setId(1L);
        item.setProductId("PROD001");
        item.setProductName("Cerveja Brahma");
        item.setQuantity(10);
        item.setPrice(new BigDecimal("4.50"));
        
        
        assertEquals(1L, item.getId());
        assertEquals("PROD001", item.getProductId());
        assertEquals("Cerveja Brahma", item.getProductName());
        assertEquals(10, item.getQuantity());
        assertEquals(new BigDecimal("4.50"), item.getPrice());
    }
    
    @Test
    void constructor_DefaultValues() {
        
        OrderItem item = new OrderItem();
        
        
        assertNull(item.getId());
        assertNull(item.getProductId());
        assertNull(item.getProductName());
        assertNull(item.getQuantity());
        assertNull(item.getPrice());
    }
} 
