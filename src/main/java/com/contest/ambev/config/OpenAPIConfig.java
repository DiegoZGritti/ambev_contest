package com.contest.ambev.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI ambevOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor local de desenvolvimento");

        Contact contact = new Contact()
                .name("Ambev Tech")
                .email("tech@ambev.com.br");

        Info info = new Info()
                .title("API de Pedidos Ambev")
                .version("1.0")
                .description("API para gerenciamento de pedidos da Ambev")
                .contact(contact)
                .license(new License().name("Apache 2.0"));

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
} 
