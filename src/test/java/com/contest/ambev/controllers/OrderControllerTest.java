package com.contest.ambev.controllers;

import com.contest.ambev.interfaces.dto.OrderItemRequestDTO;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import com.contest.ambev.usecases.CreateOrderUseCase;
import com.contest.ambev.usecases.GetAllOrdersUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = OrderControllerTest.TestConfig.class)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "server.port=8081",
    "app.features.sqs-enabled=false"
})
public class OrderControllerTest {

    @TestConfiguration
    @Import(OrderController.class)
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateOrderUseCase createOrderUseCase;
    
    @MockBean
    private GetAllOrdersUseCase getAllOrdersUseCase;

    private OrderRequestDTO requestDTO;
    private OrderResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("PROD-001");
        itemDTO.setDescription("Test Product");
        itemDTO.setQuantity(5);
        itemDTO.setUnitPrice(new BigDecimal("10.0"));

        requestDTO = new OrderRequestDTO();
        requestDTO.setExternalId("TEST-ORDER-001");
        requestDTO.setItems(Collections.singletonList(itemDTO));

        
        responseDTO = new OrderResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setExternalId("TEST-ORDER-001");
        responseDTO.setTotalValue(new BigDecimal("50.0"));
    }

    @Test
    void createOrder_Success() throws Exception {
        
        when(createOrderUseCase.execute(any(OrderRequestDTO.class))).thenReturn(responseDTO);

        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.externalId", is("TEST-ORDER-001")))
                .andExpect(jsonPath("$.totalValue", is(50.0)));

        
        verify(createOrderUseCase).execute(any(OrderRequestDTO.class));
    }

    @Test
    void getAllOrders_Success() throws Exception {
        
        List<OrderResponseDTO> orders = Collections.singletonList(responseDTO);
        when(getAllOrdersUseCase.execute()).thenReturn(orders);

        
        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].externalId", is("TEST-ORDER-001")))
                .andExpect(jsonPath("$[0].totalValue", is(50.0)));

        
        verify(getAllOrdersUseCase).execute();
    }

    @Test
    void createOrder_BadRequest() throws Exception {
        
        OrderRequestDTO invalidRequest = new OrderRequestDTO();
        invalidRequest.setItems(requestDTO.getItems());

        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_BadRequest_NoItems() throws Exception {
        
        OrderRequestDTO invalidRequest = new OrderRequestDTO();
        invalidRequest.setExternalId("TEST-ORDER-001");
        invalidRequest.setItems(Collections.emptyList());

        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
} 
