package com.contest.ambev.usecases.impl;

import com.contest.ambev.domain.entity.Order;
import com.contest.ambev.domain.entity.OrderStatus;
import com.contest.ambev.domain.mapper.DomainOrderMapper;
import com.contest.ambev.domain.service.OrderCalculationService;
import com.contest.ambev.interfaces.dto.OrderRequestDTO;
import com.contest.ambev.interfaces.dto.OrderResponseDTO;
import com.contest.ambev.ports.output.OrderEventPublisher;
import com.contest.ambev.ports.output.OrderRepository;
import com.contest.ambev.ports.output.ServiceBPort;
import com.contest.ambev.usecases.CreateOrderUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOrderUseCaseImpl implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final DomainOrderMapper orderMapper;
    private final OrderCalculationService orderCalculationService;
    private final OrderEventPublisher orderEventPublisher;
    private final ServiceBPort serviceBPort;
    
    @Value("${app.features.sqs-enabled:false}")
    private boolean sqsEnabled;

    
    @Override
    @Transactional
    public OrderResponseDTO execute(OrderRequestDTO requestDTO) {
        log.info("Iniciando processamento do pedido: {}", requestDTO.getExternalId());
        
        
        if (orderRepository.existsByExternalId(requestDTO.getExternalId())) {
            log.warn("Pedido já existe: {}", requestDTO.getExternalId());
            throw new RuntimeException("Pedido com ID externo " + requestDTO.getExternalId() + " já existe");
        }
        
        
        Order order = orderMapper.toEntity(requestDTO);
        
        
        order.setStatus(OrderStatus.RECEBIDO);
        order.setCreatedAt(LocalDateTime.now());
        
        
        order.setTotalValue(orderCalculationService.calculateTotalValue(order));
        
        if (sqsEnabled) {
            
            log.info("SQS habilitado - Enviando pedido para fila de processamento: {}", order.getExternalId());
            orderEventPublisher.publishOrderReceived(requestDTO);
            
            
            OrderResponseDTO responseDTO = orderMapper.toResponseDTO(order);
            log.info("Pedido enviado para processamento: {}", responseDTO.getExternalId());
            return responseDTO;
        } else {
            
            log.info("SQS desabilitado - Salvando pedido diretamente no banco: {}", order.getExternalId());
            
            
            Order savedOrder = orderRepository.save(order);
            log.info("Pedido salvo no banco com sucesso: {}", savedOrder.getExternalId());
            
            
            orderEventPublisher.publishOrderReceived(requestDTO);
            
            try {
                
                savedOrder.setStatus(OrderStatus.PROCESSANDO);
                savedOrder = orderRepository.save(savedOrder);
                log.info("Status do pedido atualizado para PROCESSANDO: {}", savedOrder.getExternalId());
                
                
                log.info("Enviando pedido para o Serviço B: {}", savedOrder.getExternalId());
                boolean sentToServiceB = serviceBPort.sendOrderToServiceB(savedOrder);
                
                if (sentToServiceB) {
                    
                    savedOrder.setStatus(OrderStatus.ENVIADO);
                    savedOrder = orderRepository.save(savedOrder);
                    log.info("Pedido enviado com sucesso para o Serviço B. Status atualizado para ENVIADO: {}", 
                            savedOrder.getExternalId());
                } else {
                    
                    savedOrder.setStatus(OrderStatus.ERRO);
                    savedOrder = orderRepository.save(savedOrder);
                    log.error("Falha ao enviar pedido para o Serviço B. Status atualizado para ERRO: {}", 
                            savedOrder.getExternalId());
                }
            } catch (Exception e) {
                
                savedOrder.setStatus(OrderStatus.ERRO);
                savedOrder = orderRepository.save(savedOrder);
                log.error("Erro ao processar pedido: {}. Mensagem: {}", savedOrder.getExternalId(), e.getMessage(), e);
            }
            
            
            orderEventPublisher.publishOrderProcessed(savedOrder);
            
            
            OrderResponseDTO responseDTO = orderMapper.toResponseDTO(savedOrder);
            log.info("Pedido processado com sucesso: {}", responseDTO.getExternalId());
            return responseDTO;
        }
    }
} 
