package com.contest.ambev.domain.rules;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderCalculationRulesTest {

    @Test
    void calculateTotalValue_SingleItem_CorrectTotalValue() {
        
        OrderItem item = new OrderItem();
        item.setProductId("PROD001");
        item.setProductName("Cerveja Brahma");
        item.setQuantity(10);
        item.setPrice(new BigDecimal("4.50"));

        Order order = new Order();
        order.setExternalId("EXT123");
        order.setItems(Collections.singletonList(item));

        
        OrderCalculationRules.calculateTotalValue(order);

        
        assertEquals(new BigDecimal("45.00"), order.getTotalValue());
    }

    @Test
    void calculateTotalValue_MultipleItems_CorrectTotalValue() {
        
        OrderItem item1 = new OrderItem();
        item1.setProductId("PROD001");
        item1.setProductName("Cerveja Brahma");
        item1.setQuantity(10);
        item1.setPrice(new BigDecimal("4.50"));

        OrderItem item2 = new OrderItem();
        item2.setProductId("PROD002");
        item2.setProductName("Cerveja Skol");
        item2.setQuantity(5);
        item2.setPrice(new BigDecimal("3.80"));

        Order order = new Order();
        order.setExternalId("EXT123");
        order.setItems(Arrays.asList(item1, item2));

        
        OrderCalculationRules.calculateTotalValue(order);

        
        assertEquals(new BigDecimal("64.00"), order.getTotalValue());
    }

    @Test
    void calculateTotalValue_NoItems_ZeroTotalValue() {
        
        Order order = new Order();
        order.setExternalId("EXT123");
        order.setItems(Collections.emptyList());

        
        OrderCalculationRules.calculateTotalValue(order);

        
        assertEquals(BigDecimal.ZERO, order.getTotalValue());
    }
    
    @Test
    void calculateTotalValue_ItemWithZeroQuantity_ExcludesFromTotal() {
        
        OrderItem item1 = new OrderItem();
        item1.setProductId("PROD001");
        item1.setProductName("Cerveja Brahma");
        item1.setQuantity(0); 
        item1.setPrice(new BigDecimal("4.50"));

        OrderItem item2 = new OrderItem();
        item2.setProductId("PROD002");
        item2.setProductName("Cerveja Skol");
        item2.setQuantity(5);
        item2.setPrice(new BigDecimal("3.80"));

        Order order = new Order();
        order.setExternalId("EXT123");
        order.setItems(Arrays.asList(item1, item2));

        
        OrderCalculationRules.calculateTotalValue(order);

        
        assertEquals(new BigDecimal("19.00"), order.getTotalValue());
    }
    
    @Test
    void calculateTotalValue_ItemWithNullQuantity_ExcludesFromTotal() {
        
        OrderItem item1 = new OrderItem();
        item1.setProductId("PROD001");
        item1.setProductName("Cerveja Brahma");
        item1.setQuantity(null); 
        item1.setPrice(new BigDecimal("4.50"));

        OrderItem item2 = new OrderItem();
        item2.setProductId("PROD002");
        item2.setProductName("Cerveja Skol");
        item2.setQuantity(5);
        item2.setPrice(new BigDecimal("3.80"));

        Order order = new Order();
        order.setExternalId("EXT123");
        order.setItems(Arrays.asList(item1, item2));

        
        OrderCalculationRules.calculateTotalValue(order);

        
        assertEquals(new BigDecimal("19.00"), order.getTotalValue());
    }
    
    @Test
    void calculateTotalValue_ItemWithNullPrice_ExcludesFromTotal() {
        
        OrderItem item1 = new OrderItem();
        item1.setProductId("PROD001");
        item1.setProductName("Cerveja Brahma");
        item1.setQuantity(10);
        item1.setPrice(null); 

        OrderItem item2 = new OrderItem();
        item2.setProductId("PROD002");
        item2.setProductName("Cerveja Skol");
        item2.setQuantity(5);
        item2.setPrice(new BigDecimal("3.80"));

        Order order = new Order();
        order.setExternalId("EXT123");
        order.setItems(Arrays.asList(item1, item2));

        
        OrderCalculationRules.calculateTotalValue(order);

        
        assertEquals(new BigDecimal("19.00"), order.getTotalValue());
    }
} 
