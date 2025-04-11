package com.contest.ambev.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
@Slf4j
@ConditionalOnProperty(name = "app.features.wiremock-enabled", havingValue = "true", matchIfMissing = true)
public class WireMockConfig {

    @Value("${service-b.port:0}")
    private int serviceBPort;

    private WireMockServer wireMockServer;

    @PostConstruct
    public void setupWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
        
        int actualPort = wireMockServer.port();
        log.info("WireMock iniciado na porta {} para simular o Serviço B", actualPort);
        
        wireMockServer.stubFor(
            post(urlEqualTo("/service-b/orders"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\":\"RECEIVED\",\"message\":\"Order received by Service B\"}")
                )
        );
        
        log.info("Stub configurado para POST /service-b/orders");
    }
    
    @PreDestroy
    public void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            log.info("WireMock parado");
        }
    }
    
    @Bean
    public WireMockServer wireMockServer() {
        return wireMockServer;
    }
} 
