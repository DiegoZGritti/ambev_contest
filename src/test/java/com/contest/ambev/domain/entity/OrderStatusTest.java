package com.contest.ambev.domain.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderStatusTest {

    @Test
    void testEnumValues() {
        OrderStatus[] statuses = OrderStatus.values();
        assertEquals(5, statuses.length);
        
        assertTrue(containsStatus(statuses, "RECEBIDO"));
        assertTrue(containsStatus(statuses, "PROCESSANDO"));
        assertTrue(containsStatus(statuses, "PROCESSADO"));
        assertTrue(containsStatus(statuses, "ENVIADO"));
        assertTrue(containsStatus(statuses, "ERRO"));
    }
    
    @Test
    void testValueOf() {
        assertEquals(OrderStatus.RECEBIDO, OrderStatus.valueOf("RECEBIDO"));
        assertEquals(OrderStatus.PROCESSANDO, OrderStatus.valueOf("PROCESSANDO"));
        assertEquals(OrderStatus.PROCESSADO, OrderStatus.valueOf("PROCESSADO"));
        assertEquals(OrderStatus.ENVIADO, OrderStatus.valueOf("ENVIADO"));
        assertEquals(OrderStatus.ERRO, OrderStatus.valueOf("ERRO"));
        
        assertThrows(IllegalArgumentException.class, () -> OrderStatus.valueOf("INVALID_STATUS"));
    }
    
    @Test
    void testOrdinal() {
        assertEquals(0, OrderStatus.RECEBIDO.ordinal());
        assertEquals(1, OrderStatus.PROCESSANDO.ordinal());
        assertEquals(2, OrderStatus.PROCESSADO.ordinal());
        assertEquals(3, OrderStatus.ENVIADO.ordinal());
        assertEquals(4, OrderStatus.ERRO.ordinal());
    }
    
    @Test
    void testToString() {
        assertEquals("RECEBIDO", OrderStatus.RECEBIDO.toString());
        assertEquals("PROCESSANDO", OrderStatus.PROCESSANDO.toString());
        assertEquals("PROCESSADO", OrderStatus.PROCESSADO.toString());
        assertEquals("ENVIADO", OrderStatus.ENVIADO.toString());
        assertEquals("ERRO", OrderStatus.ERRO.toString());
    }
    
    private boolean containsStatus(OrderStatus[] statuses, String name) {
        for (OrderStatus status : statuses) {
            if (status.name().equals(name)) {
                return true;
            }
        }
        return false;
    }
} 
