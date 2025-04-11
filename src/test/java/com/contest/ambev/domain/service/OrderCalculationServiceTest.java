package com.contest.ambev.domain.service;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCalculationServiceTest {

    private OrderCalculationService orderCalculationService;
    private Order order;

    @BeforeEach
    void setUp() {
        orderCalculationService = new OrderCalculationService();
        order = new Order();
    }

    @Test
    void calculateTotalValue_EmptyOrderItems_ShouldReturnZero() {
        
        order.setItems(Collections.emptyList());

        
        BigDecimal result = orderCalculationService.calculateTotalValue(order);

        
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void calculateTotalValue_NullOrderItems_ShouldReturnZero() {
        
        order.setItems(null);

        
        BigDecimal result = orderCalculationService.calculateTotalValue(order);

        
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void calculateTotalValue_SingleItem_ShouldReturnCorrectTotal() {
        
        OrderItem item = new OrderItem();
        item.setQuantity(5);
        item.setPrice(new BigDecimal("10.50"));
        order.setItems(Collections.singletonList(item));

        
        BigDecimal result = orderCalculationService.calculateTotalValue(order);

        
        assertEquals(new BigDecimal("52.50").setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void calculateTotalValue_MultipleItems_ShouldReturnCorrectTotal() {
        
        OrderItem item1 = new OrderItem();
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("15.75"));

        OrderItem item2 = new OrderItem();
        item2.setQuantity(3);
        item2.setPrice(new BigDecimal("8.25"));

        OrderItem item3 = new OrderItem();
        item3.setQuantity(1);
        item3.setPrice(new BigDecimal("99.99"));

        order.setItems(Arrays.asList(item1, item2, item3));

        
        BigDecimal result = orderCalculationService.calculateTotalValue(order);

        
        
        assertEquals(new BigDecimal("156.24").setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void calculateTotalValue_ItemWithNullValues_ShouldHandleGracefully() {
        
        OrderItem item1 = new OrderItem();
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("15.75"));

        OrderItem item2 = new OrderItem();
        

        OrderItem item3 = new OrderItem();
        item3.setQuantity(null);
        item3.setPrice(new BigDecimal("10.00"));

        OrderItem item4 = new OrderItem();
        item4.setQuantity(5);
        item4.setPrice(null);

        order.setItems(Arrays.asList(item1, item2, item3, item4));

        
        BigDecimal result = orderCalculationService.calculateTotalValue(order);

        
        
        assertEquals(new BigDecimal("31.50").setScale(2, RoundingMode.HALF_UP), result);
    }

    @Test
    void calculateTotalValue_RoundingToTwoDecimals() {
        
        OrderItem item = new OrderItem();
        item.setQuantity(3);
        item.setPrice(new BigDecimal("1.333"));
        order.setItems(Collections.singletonList(item));

        
        BigDecimal result = orderCalculationService.calculateTotalValue(order);

        
        
        assertEquals(new BigDecimal("4.00").setScale(2, RoundingMode.HALF_UP), result);
        assertEquals(2, result.scale());
    }
} 
