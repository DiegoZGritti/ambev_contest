package com.contest.ambev.adapters.outbound;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceBClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ServiceBClient serviceBClient;

    private Order order;
    private Map<String, Object> responseBody;

    @BeforeEach
    void setUp() {
        
        ReflectionTestUtils.setField(serviceBClient, "serviceBDefaultUrl", "http://localhost:9091/service-b");

        order = new Order();
        order.setId(1L);
        order.setExternalId("TEST-ORDER-123");
        order.setTotalValue(new BigDecimal("125.50"));

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId("PROD-001");
        item.setProductName("Test Product");
        item.setQuantity(5);
        item.setPrice(new BigDecimal("25.10"));

        order.setItems(Collections.singletonList(item));

        responseBody = new HashMap<>();
        responseBody.put("status", "RECEIVED");
        responseBody.put("message", "Order received by Service B");
    }

    @Test
    void sendOrderToServiceB_Success() {
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(responseEntity);

        boolean result = serviceBClient.sendOrderToServiceB(order);

        assertTrue(result);
    }

    @Test
    void sendOrderToServiceB_HttpError() {
        
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = serviceBClient.sendOrderToServiceB(order);

        assertFalse(result);
    }

    @Test
    void sendOrderToServiceB_Exception() {
        
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = serviceBClient.sendOrderToServiceB(order);

        assertFalse(result);
    }
} 
