package com.contest.ambev.infrastructure.config;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderItem;
import com.contest.ambev.domain.entity.OrderStatus;
import com.contest.ambev.domain.service.OrderCalculationService;
import com.contest.ambev.ports.output.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;


@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer {
    
    private final OrderRepository orderRepository;
    private final OrderCalculationService orderCalculationService;
    
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (orderRepository.count() > 0) {
                log.info("Banco de dados já contém dados. Pulando inicialização.");
                return;
            }
            
            log.info("Inicializando dados de teste...");
            
            
            OrderItem item1 = new OrderItem();
            item1.setProductId("PROD001");
            item1.setProductName("Cerveja Brahma 600ml");
            item1.setQuantity(10);
            item1.setPrice(new BigDecimal("8.90"));
            
            OrderItem item2 = new OrderItem();
            item2.setProductId("PROD002");
            item2.setProductName("Cerveja Skol 350ml");
            item2.setQuantity(24);
            item2.setPrice(new BigDecimal("3.50"));
            
            Order order1 = new Order();
            order1.setExternalId("EXT-001");
            order1.setItems(Arrays.asList(item1, item2));
            order1.setStatus(OrderStatus.PROCESSADO);
            order1.setCreatedAt(LocalDateTime.now().minusDays(1));
            order1.setTotalValue(orderCalculationService.calculateTotalValue(order1));
            
            
            OrderItem item3 = new OrderItem();
            item3.setProductId("PROD003");
            item3.setProductName("Cerveja Antarctica Original 600ml");
            item3.setQuantity(5);
            item3.setPrice(new BigDecimal("9.90"));
            
            Order order2 = new Order();
            order2.setExternalId("EXT-002");
            order2.setItems(Arrays.asList(item3));
            order2.setStatus(OrderStatus.RECEBIDO);
            order2.setCreatedAt(LocalDateTime.now().minusHours(2));
            order2.setTotalValue(orderCalculationService.calculateTotalValue(order2));
            
            
            orderRepository.save(order1);
            orderRepository.save(order2);
            
            log.info("Dados de teste inicializados com sucesso!");
        };
    }
} 
