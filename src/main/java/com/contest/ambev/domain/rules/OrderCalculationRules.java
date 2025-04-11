package com.contest.ambev.domain.rules;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;

import java.math.BigDecimal;
import java.util.Objects;

public class OrderCalculationRules {

    public static void calculateTotalValue(Order order) {
        BigDecimal total = order.getItems().stream()
                .filter(item -> item.getQuantity() != null && item.getQuantity() > 0)
                .filter(item -> item.getPrice() != null)
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalValue(total);
    }
} 
