package com.contest.ambev.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAPIConfigTest {

    private OpenAPIConfig openAPIConfig;

    @BeforeEach
    void setUp() {
        openAPIConfig = new OpenAPIConfig();
        ReflectionTestUtils.setField(openAPIConfig, "serverPort", "8080");
    }

    @Test
    void ambevOpenAPI_ShouldReturnCorrectlyConfiguredOpenAPI() {
        
        OpenAPI openAPI = openAPIConfig.ambevOpenAPI();

        assertNotNull(openAPI);
        
        assertNotNull(openAPI.getInfo());
        assertEquals("API de Pedidos Ambev", openAPI.getInfo().getTitle());
        assertEquals("1.0", openAPI.getInfo().getVersion());
        assertTrue(openAPI.getInfo().getDescription().contains("pedidos"));
        
        assertNotNull(openAPI.getInfo().getContact());
        assertEquals("Ambev Tech", openAPI.getInfo().getContact().getName());
        assertEquals("tech@ambev.com.br", openAPI.getInfo().getContact().getEmail());
        
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("Apache 2.0", openAPI.getInfo().getLicense().getName());
        
        assertNotNull(openAPI.getServers());
        assertEquals(1, openAPI.getServers().size());
        assertEquals("http://localhost:8080", openAPI.getServers().get(0).getUrl());
        assertEquals("Servidor local de desenvolvimento", openAPI.getServers().get(0).getDescription());
    }
} 
