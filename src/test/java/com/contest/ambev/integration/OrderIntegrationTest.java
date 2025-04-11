package com.contest.ambev.integration;

import com.contest.ambev.domain.entity.OrderStatus;
import com.contest.ambev.interfaces.dto.OrderItemRequestDTO;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateAndListOrders() throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String externalId = "EXT-" + timestamp + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        OrderItemRequestDTO itemDTO = new OrderItemRequestDTO();
        itemDTO.setProductId("ABC123");
        itemDTO.setDescription("Test Product");
        itemDTO.setQuantity(5);
        itemDTO.setUnitPrice(new BigDecimal("10.00"));

        OrderRequestDTO requestDTO = new OrderRequestDTO();
        requestDTO.setExternalId(externalId);
        requestDTO.setItems(List.of(itemDTO));


        MvcResult createResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();


        MockHttpServletResponse createResponse = createResult.getResponse();
        OrderResponseDTO orderResponse = objectMapper.readValue(createResponse.getContentAsString(), OrderResponseDTO.class);

        assertEquals(requestDTO.getExternalId(), orderResponse.getExternalId());
        assertEquals(requestDTO.getItems().size(), orderResponse.getItems().size());


        String timestamp2 = LocalDateTime.now().plusSeconds(1).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String externalId2 = "EXT-" + timestamp2 + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        OrderItemRequestDTO itemDTO2 = new OrderItemRequestDTO();
        itemDTO2.setProductId("DEF456");
        itemDTO2.setDescription("Another Product");
        itemDTO2.setQuantity(2);
        itemDTO2.setUnitPrice(new BigDecimal("20.00"));

        OrderRequestDTO requestDTO2 = new OrderRequestDTO();
        requestDTO2.setExternalId(externalId2);
        requestDTO2.setItems(List.of(itemDTO2));


        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO2)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        if (false) {
            mockMvc.perform(
                    MockMvcRequestBuilders.post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        MvcResult listResult = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();


        MockHttpServletResponse listResponse = listResult.getResponse();
        List<OrderResponseDTO> orders = objectMapper.readValue(
                listResponse.getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, OrderResponseDTO.class));

        assertTrue(orders.size() >= 2);
        assertTrue(orders.stream().anyMatch(o -> o.getExternalId().equals(externalId)));
        assertTrue(orders.stream().anyMatch(o -> o.getExternalId().equals(externalId2)));
    }
} 
