# Ambev Order Service API

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Version](https://img.shields.io/badge/Version-1.0.0-blue.svg)
![AWS](https://img.shields.io/badge/AWS-SQS-blue.svg)
![WireMock](https://img.shields.io/badge/WireMock-3.3.1-purple.svg)

API REST de processamento de pedidos usando arquitetura hexagonal, com prevenção de duplicação de pedidos por ID externo, persistência em banco de dados e integração com serviços de mensageria e externos.

## 📋 Índice

- [Arquitetura](#-arquitetura)
- [Fluxos de Operações](#-fluxos-de-operações)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Como Executar](#-como-executar)
- [Endpoints da API](#-endpoints-da-api)
- [Exemplos de Pedidos para Teste](#-exemplos-de-pedidos-para-teste)
- [Testes da API](#-testes-da-api)
- [Monitoramento e Troubleshooting](#-monitoramento-e-troubleshooting)

## 🏗 Arquitetura

O projeto segue os princípios da **Arquitetura Hexagonal** (Ports and Adapters), organizando o código em camadas bem definidas:

```
+--------------------------------------------------------------+
|                                                              |
|  +------------------+        +---------------------------+   |
|  |                  |        |                           |   |
|  |     Domínio      |        |         Aplicação        |   |
|  |                  |        |                          |   |
|  | - Entidades      |◄-------|  - Casos de Uso         |   |
|  | - Regras         |        |  - Serviços             |   |
|  | - Aggregates     |        |  - Orquestração         |   |
|  |                  |        |                          |   |
|  +------------------+        +---------------------------+   |
|        △  △                          △          |           |
|        |  |                          |          |           |
|        |  |                          |          ▼           |
|        |  |                          |  +------------------+|
|        |  |                          |  |                  ||
|        |  |       +----------------+ |  |      Portas      ||
|        |  |       |                |◄─┘  |                  ||
|        |  |       |  Adaptadores   |     | - Interfaces In  ||
|        |  └-------| de Entrada     |     | - Interfaces Out ||
|        |          |                |     |                  ||
|        |          +----------------+     +------------------+|
|        |                                          △          |
|        |                                          |          |
|        |          +----------------+              |          |
|        |          |                |              |          |
|        └─────────►|  Adaptadores   |--------------┘          |
|                   | de Saída       |                         |
|                   |                |                         |
|                   +----------------+                         |
|                                                              |
+--------------------------------------------------------------+
```

### Camadas do Projeto:

#### 1. Domain (Domínio)
- **Entidades**: `Order`, `OrderItem`, `OrderStatus`
- **Regras de Negócio**: `OrderCalculationRules`, `OrderCalculationService` 
- **Mapeadores**: `DomainOrderMapper`

#### 2. Application (Aplicação)
- **Casos de Uso**: `CreateOrderUseCase`, `GetAllOrdersUseCase`
- **Portas**: `OrderMapperPort`
- **Serviços**: `OrderServiceImpl`

#### 3. Ports (Portas)
- **Input**: `OrderUseCase`
- **Output**: `OrderRepository`, `OrderEventPublisher`, `ServiceBPort`

#### 4. Adapters (Adaptadores)
- **Input**: `OrderController`, `OrderProcessingConsumer`
- **Output**: `OrderRepositoryAdapter`, `SQSOrderEventPublisher`, `ServiceBClient`

#### 5. Infrastructure (Infraestrutura)
- **Config**: `SQSConfig`, `RestTemplateConfig`, `WireMockConfig`, `OpenAPIConfig`
- **External**: `ExternalApiClient`, `ExternalOrderService`

## 🔄 Fluxos de Operações

### Fluxo Principal: Criação de Pedido (Modo Síncrono - SQS Desabilitado)

```
┌─────────┐    ┌─────────────┐    ┌───────────────────┐    ┌────────────────┐
│ Cliente │───►│ API Gateway │───►│ OrderController   │───►│ CreateOrder    │
└─────────┘    └─────────────┘    │ (/api/orders)     │    │ UseCase        │
                                  └───────────────────┘    └──────┬─────────┘
                                                                  │
                                                                  ▼
┌────────────────────┐    ┌────────────────┐    ┌───────────────────────────┐
│ Serviço B          │◄───┤ ServiceBClient │◄───┤ 3. Envio ao serviço       │
│ (WireMock)         │    │                │    │    (após persistência)     │
└────────────────────┘    └────────────────┘    └──────────┬────────────────┘
                                                           │
                                                           ▼
  ┌────────────────────────────────────────┐    ┌────────────────────────────┐
  │ 4. Atualização do Status: ENVIADO/ERRO │───►│ Banco H2                   │
  └────────────────────────────────────────┘    │ Status sequencial:         │
                                                │ 1. RECEBIDO                │
                                                │ 2. PROCESSANDO             │
                                                │ 3. ENVIADO/ERRO            │
                                                └────────────────────────────┘
```

### Fluxo Sequencial Completo

1. **Recebimento do Pedido**: Cliente → API → Controller → Use Case
2. **Validação e Persistência Inicial**:
   - Validação de duplicação pelo ID externo
   - Salvamento no banco com status RECEBIDO
   - Cálculo do valor total
3. **Processamento**:
   - Atualização do status para PROCESSANDO no banco
   - Envio do pedido para o Serviço B (WireMock)
4. **Finalização**:
   - Atualização do status para ENVIADO (sucesso) ou ERRO (falha)
   - Retorno do resultado ao cliente

### Fluxo Alternativo: Criação de Pedido (Modo Assíncrono - SQS Habilitado)

```
┌─────────┐    ┌─────────────┐    ┌───────────────────┐    ┌───────────────┐    ┌───────────────┐
│ Cliente │───►│ API Gateway │───►│ OrderController   │───►│ CreateOrder   │───►│ SQS Publisher │
└─────────┘    └─────────────┘    │ (/api/orders)     │    │ UseCase       │    └───────┬───────┘
                                  └───────────────────┘    └───────────────┘            │
                                                                                        ▼
                           ┌────────────────────┐    ┌────────────────┐    ┌───────────────┐
                           │ 1. Status: RECEBIDO│◄───┤ Repository     │◄───┤ SQS Consumer  │
                           └────────┬───────────┘    └────────────────┘    └───────────────┘
                                    │
                                    ▼
                           ┌────────────────────┐    ┌────────────────┐
                           │ 2. PROCESSANDO     │───►│ ServiceBClient │───►┌───────────────┐
                           └────────┬───────────┘    └────────────────┘    │ Serviço B     │
                                    │                                      │ (WireMock)     │
                                    ▼                                      └───────────────┘
                           ┌────────────────────┐
                           │ 3. ENVIADO/ERRO    │
                           └────────────────────┘
                                   Banco H2    
```

## 🛠 Tecnologias Utilizadas

- **Backend**: Spring Boot 3.2.0, Java 21
- **Banco de Dados**: H2 (em memória)
- **Mensageria**: AWS SQS (suporte opcional via LocalStack)
- **Mocks**: WireMock para simulação do Serviço B
- **Documentação**: SpringDoc/Swagger
- **Mapeamento**: MapStruct
- **Validação**: Jakarta Bean Validation
- **Logs**: SLF4J + Logback

## 🚀 Como Executar

### Pré-requisitos

- Java 21+ (JDK)
- Maven 3.8+
- Docker (opcional, apenas para o modo SQS)

### Passos para Execução

1. **Clone o repositório**:
   ```bash
   git clone https://github.com/seu-usuario/ambev-contest.git
   cd ambev-contest
   ```

2. **Configure o ambiente (opcional para SQS)**:
   ```bash
   # Se quiser usar SQS, inicie o LocalStack:
   docker run -d -p 4566:4566 -p 4571:4571 --name localstack localstack/localstack
   
   # Crie as filas SQS:
   aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name create-order-queue
   aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name validate-order-queue
   aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name process-order-queue
   ```

3. **Compile o projeto**:
   ```bash
   ./mvnw clean package
   ```

4. **Execute a aplicação (modo sem SQS)**:
   ```bash
   ./mvnw spring-boot:run
   ```

   **Ou execute com SQS habilitado**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.features.sqs-enabled=true"
   ```

5. **Verifique a aplicação**:
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
     - JDBC URL: `jdbc:h2:mem:ambev`
     - Username: `sa`
     - Password: (vazio)
   - WireMock: [http://localhost:9090/__admin/](http://localhost:9090/__admin/)

## 📡 Endpoints da API

### REST API

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET    | `/api/orders` | Lista todos os pedidos processados |
| POST   | `/api/orders` | Cria um novo pedido |

### Payload para Criação de Pedido

```json
{
  "externalId": "PEDIDO-001",  // ID único externo (obrigatório)
  "items": [                   // Lista de itens (obrigatório)
    {
      "productId": "PROD-123",      // ID do produto (obrigatório)
      "description": "Cerveja Brahma 600ml", // Descrição do produto
      "quantity": 10,               // Quantidade (obrigatório)
      "unitPrice": 8.90             // Preço unitário (obrigatório)
    }
  ]
}
```

### Formato de Resposta

```json
{
  "id": 1,
  "externalId": "PEDIDO-001",
  "items": [
    {
      "id": 1,
      "productId": "PROD-123",
      "productName": "Cerveja Brahma 600ml",
      "quantity": 10,
      "price": 8.90
    }
  ],
  "status": "ENVIADO",
  "totalValue": 89.00,
  "createdAt": "2025-04-11T12:34:56"
}
```

## 📊 Exemplos de Pedidos para Teste

### 1. Pedido Simples - Cerveja Brahma
```json
{
  "externalId": "PEDIDO-001",
  "items": [
    {
      "productId": "PROD-001",
      "description": "Cerveja Brahma 600ml",
      "quantity": 12,
      "unitPrice": 8.50
    }
  ]
}
```

### 2. Pedido Misto - Cervejas Variadas
```json
{
  "externalId": "PEDIDO-002",
  "items": [
    {
      "productId": "PROD-001",
      "description": "Cerveja Brahma 600ml",
      "quantity": 6,
      "unitPrice": 8.50
    },
    {
      "productId": "PROD-002",
      "description": "Cerveja Skol 350ml",
      "quantity": 12,
      "unitPrice": 3.75
    },
    {
      "productId": "PROD-003",
      "description": "Cerveja Antarctica Original 600ml",
      "quantity": 6,
      "unitPrice": 9.20
    }
  ]
}
```

### 3. Pedido Premium - Cervejas Especiais
```json
{
  "externalId": "PEDIDO-003",
  "items": [
    {
      "productId": "PROD-005",
      "description": "Stella Artois 275ml",
      "quantity": 12,
      "unitPrice": 5.50
    },
    {
      "productId": "PROD-006",
      "description": "Corona Extra 330ml",
      "quantity": 6,
      "unitPrice": 7.30
    }
  ]
}
```

### 4. Pedido Refrigerantes
```json
{
  "externalId": "PEDIDO-004",
  "items": [
    {
      "productId": "PROD-008",
      "description": "Guaraná Antarctica 2L",
      "quantity": 6,
      "unitPrice": 6.80
    },
    {
      "productId": "PROD-011",
      "description": "Pepsi 2L",
      "quantity": 6,
      "unitPrice": 7.00
    }
  ]
}
```

### 5. Pedido Águas e Energéticos
```json
{
  "externalId": "PEDIDO-005",
  "items": [
    {
      "productId": "PROD-004",
      "description": "Água Mineral 500ml",
      "quantity": 24,
      "unitPrice": 2.00
    },
    {
      "productId": "PROD-012",
      "description": "H2OH! 500ml",
      "quantity": 12,
      "unitPrice": 3.60
    },
    {
      "productId": "PROD-013",
      "description": "Gatorade 500ml",
      "quantity": 6,
      "unitPrice": 4.50
    }
  ]
}
```

### 6. Pedido Cervejas Alemãs
```json
{
  "externalId": "PEDIDO-006",
  "items": [
    {
      "productId": "PROD-009",
      "description": "Beck's 330ml",
      "quantity": 12,
      "unitPrice": 6.90
    },
    {
      "productId": "PROD-010",
      "description": "Spaten 350ml",
      "quantity": 12,
      "unitPrice": 5.25
    }
  ]
}
```

### 7. Pedido Budweiser
```json
{
  "externalId": "PEDIDO-007",
  "items": [
    {
      "productId": "PROD-007",
      "description": "Budweiser 350ml",
      "quantity": 24,
      "unitPrice": 4.75
    }
  ]
}
```

### 8. Pedido Misto Grande
```json
{
  "externalId": "PEDIDO-008",
  "items": [
    {
      "productId": "PROD-001",
      "description": "Cerveja Brahma 600ml",
      "quantity": 12,
      "unitPrice": 8.50
    },
    {
      "productId": "PROD-002",
      "description": "Cerveja Skol 350ml",
      "quantity": 24,
      "unitPrice": 3.75
    },
    {
      "productId": "PROD-004",
      "description": "Água Mineral 500ml",
      "quantity": 12,
      "unitPrice": 2.00
    },
    {
      "productId": "PROD-008",
      "description": "Guaraná Antarctica 2L",
      "quantity": 6,
      "unitPrice": 6.80
    }
  ]
}
```

### 9. Pedido Duplicado (para testar validação)
```json
{
  "externalId": "PEDIDO-001",
  "items": [
    {
      "productId": "PROD-001",
      "description": "Cerveja Brahma 600ml",
      "quantity": 12,
      "unitPrice": 8.50
    }
  ]
}
```

### 10. Pedido com ID Único
```json
{
  "externalId": "PEDIDO-010",
  "items": [
    {
      "productId": "PROD-005",
      "description": "Stella Artois 275ml",
      "quantity": 18,
      "unitPrice": 5.50
    },
    {
      "productId": "PROD-004",
      "description": "Água Mineral 500ml",
      "quantity": 12,
      "unitPrice": 2.00
    }
  ]
}
```

## 🧪 Testes da API

### 1. Via Swagger UI

1. Acesse `http://localhost:8080/swagger-ui.html`
2. Explore e teste os endpoints disponíveis

### 2. Via cURL

#### Criar Pedido
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "externalId": "PEDIDO-CURL-001",
    "items": [
      {
        "productId": "PROD-001",
        "description": "Cerveja Brahma 600ml",
        "quantity": 6,
        "unitPrice": 8.50
      }
    ]
  }'
```

#### Listar Pedidos
```bash
curl -X GET http://localhost:8080/api/orders
```

### 3. Via Console H2

1. Acesse `http://localhost:8080/h2-console`
2. Conecte usando:
   - JDBC URL: `jdbc:h2:mem:ambev`
   - Username: `sa`
   - Password: (vazio)
3. Consulte as tabelas:
   ```sql
   SELECT * FROM ORDERS;
   SELECT * FROM ORDER_ITEMS;
   ```

### 4. Consultando Status de Pedidos no Banco

```sql
-- Consulta para verificar a evolução do status dos pedidos
SELECT
    id,
    external_id,
    status,
    total_value,
    created_at
FROM
    orders
ORDER BY
    created_at DESC;
```

## 📈 Monitoramento e Troubleshooting

### Logs da Aplicação

A aplicação usa logging estruturado e detalhado para facilitar o diagnóstico de problemas:

```
2025-04-11 01:49:51.921 INFO  [ambev-order-service] --- OrderController: Recebendo pedido com ID externo: PEDIDO-001
2025-04-11 01:49:51.927 INFO  [ambev-order-service] --- CreateOrderUseCaseImpl: SQS desabilitado - Salvando pedido diretamente no banco: PEDIDO-001
2025-04-11 01:49:51.932 INFO  [ambev-order-service] --- CreateOrderUseCaseImpl: Pedido salvo no banco com sucesso: PEDIDO-001
2025-04-11 01:49:51.952 INFO  [ambev-order-service] --- ConsoleOrderEventPublisher: SQS desabilitado - Evento de pedido recebido simulado para o ID externo: PEDIDO-001
2025-04-11 01:49:51.955 INFO  [ambev-order-service] --- CreateOrderUseCaseImpl: Status do pedido atualizado para PROCESSANDO: PEDIDO-001
2025-04-11 01:49:51.958 INFO  [ambev-order-service] --- ServiceBClient: Enviando pedido PEDIDO-001 para o Serviço B em http://localhost:9090/service-b/orders
2025-04-11 01:49:51.981 INFO  [ambev-order-service] --- ServiceBClient: Pedido PEDIDO-001 enviado com sucesso para o Serviço B. Resposta: {status=RECEIVED, message=Order received by Service B}
2025-04-11 01:49:51.985 INFO  [ambev-order-service] --- CreateOrderUseCaseImpl: Pedido enviado com sucesso para o Serviço B. Status atualizado para ENVIADO: PEDIDO-001
2025-04-11 01:49:51.991 INFO  [ambev-order-service] --- ConsoleOrderEventPublisher: SQS desabilitado - Evento de pedido processado simulado para o ID externo: PEDIDO-001
2025-04-11 01:49:51.999 INFO  [ambev-order-service] --- CreateOrderUseCaseImpl: Pedido processado com sucesso: PEDIDO-001
```

### Status dos Serviços

Você pode verificar o status dos serviços:

- **WireMock UI**: http://localhost:9090/__admin/
- **H2 Database**: http://localhost:8080/h2-console
- **LocalStack (se habilitado)**: http://localhost:4566/health 