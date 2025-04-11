package com.contest.ambev.domain.service;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;


@Service
@Slf4j
public class OrderCalculationService {
    
    
    public BigDecimal calculateTotalValue(Order order) {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            log.warn("Tentativa de cálculo em pedido sem itens");
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        
        BigDecimal total = order.getItems().stream()
                .filter(item -> item.getQuantity() != null && item.getPrice() != null)
                .map(this::calculateItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return total.setScale(2, RoundingMode.HALF_UP);
    }
    
    
    private BigDecimal calculateItemTotal(OrderItem item) {
        if (item.getQuantity() == null || item.getPrice() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        
        return item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);
    }
} 
