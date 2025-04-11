package com.contest.ambev.domain.exception;


public class OrderDuplicatedException extends RuntimeException {
    
    public OrderDuplicatedException(String externalId) {
        super("Pedido com ID externo " + externalId + " já existe");
    }
} 
