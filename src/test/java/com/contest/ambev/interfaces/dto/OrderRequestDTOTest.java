package com.contest.ambev.interfaces.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OrderRequestDTOTest {
    
    private Validator validator;
    
    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    public void testValidOrderRequest() {
        
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("PROD001");
        itemDTO.setDescription("Cerveja Brahma");
        itemDTO.setQuantity(10);
        itemDTO.setUnitPrice(new BigDecimal("4.50"));
        
        
        OrderRequestDTO orderDTO = new OrderRequestDTO();
        orderDTO.setExternalId("EXT-ORDER-123");
        orderDTO.setItems(Collections.singletonList(itemDTO));
        
        
        Set<ConstraintViolation<OrderRequestDTO>> violations = validator.validate(orderDTO);
        assertTrue(violations.isEmpty(), "Deve passar na validação sem violações");
    }
    
    @Test
    public void testOrderWithoutExternalId() {
        
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("PROD002");
        itemDTO.setDescription("Cerveja Skol");
        itemDTO.setQuantity(5);
        itemDTO.setUnitPrice(new BigDecimal("3.80"));
        
        
        OrderRequestDTO orderDTO = new OrderRequestDTO();
        orderDTO.setItems(Collections.singletonList(itemDTO));
        
        
        Set<ConstraintViolation<OrderRequestDTO>> violations = validator.validate(orderDTO);
        assertFalse(violations.isEmpty(), "Deve falhar na validação");
        
        
        boolean hasExternalIdValidation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("externalId"));
        
        assertTrue(hasExternalIdValidation, "Deve conter validação para externalId");
    }
    
    @Test
    public void testOrderWithoutItems() {
        
        OrderRequestDTO orderDTO = new OrderRequestDTO();
        orderDTO.setExternalId("EXT-ORDER-456");
        
        
        Set<ConstraintViolation<OrderRequestDTO>> violations = validator.validate(orderDTO);
        assertFalse(violations.isEmpty(), "Deve falhar na validação");
        
        
        boolean hasItemsValidation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("items"));
        
        assertTrue(hasItemsValidation, "Deve conter validação para items");
    }
    
    @Test
    public void testOrderWithEmptyItems() {
        
        OrderRequestDTO orderDTO = new OrderRequestDTO();
        orderDTO.setExternalId("EXT-ORDER-789");
        orderDTO.setItems(List.of());
        
        
        Set<ConstraintViolation<OrderRequestDTO>> violations = validator.validate(orderDTO);
        assertFalse(violations.isEmpty(), "Deve falhar na validação");
        
        
        boolean hasItemsValidation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("items"));
        
        assertTrue(hasItemsValidation, "Deve conter validação para items vazios");
    }
    
    @Test
    public void testOrderWithInvalidItem() {
        
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("PROD003");
        itemDTO.setDescription("Água Mineral");
        itemDTO.setUnitPrice(new BigDecimal("2.00"));
        
        
        OrderRequestDTO orderDTO = new OrderRequestDTO();
        orderDTO.setExternalId("EXT-ORDER-999");
        orderDTO.setItems(Collections.singletonList(itemDTO));
        
        
        Set<ConstraintViolation<OrderRequestDTO>> violations = validator.validate(orderDTO);
        

        assertFalse(violations.isEmpty(), "Deve falhar na validação devido ao @Valid nos itens");
        

        boolean hasQuantityValidation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().contains("items") && 
                               v.getPropertyPath().toString().contains("quantity"));
        
        assertTrue(hasQuantityValidation, "Deve validar o campo quantity do item");
    }
} 
