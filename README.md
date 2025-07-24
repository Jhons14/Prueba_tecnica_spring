# Microservices Project: Products & Inventory Management

A comprehensive microservices architecture implementation featuring product management and inventory control with purchase processing capabilities.

## Table of Contents

- [Architecture Overview](#architecture-overview)
- [Technical Stack](#technical-stack)
- [Services Description](#services-description)
- [Purchase Flow Design](#purchase-flow-design)
- [Installation & Setup](#installation--setup)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Technical Decisions](#technical-decisions)
- [AI Tools Usage](#ai-tools-usage)

## Architecture Overview

```
┌─────────────────┐    HTTP/JSON API    ┌─────────────────┐
│                 │ ◄──────────────────► │                 │
│  Products       │                      │   Inventory     │
│  Service        │                      │   Service       │
│  (Port 8080)    │                      │  (Port 8081)    │
│                 │                      │                 │
└─────────────────┘                      └─────────────────┘
        │                                          │
        │                                          │
        ▼                                          ▼
┌─────────────────┐                      ┌─────────────────┐
│   H2 Database   │                      │   H2 Database   │
│   (Products)    │                      │ (Inventory &    │
│                 │                      │  Purchases)     │
└─────────────────┘                      └─────────────────┘
```

### Service Communication Flow

```
Client Request ──► Inventory Service ──► Products Service
                         │                       │
                         ▼                       ▼
                  Purchase Processing    Product Information
                         │                       │
                         ▼                       │
                  Inventory Update ◄─────────────┘
                         │
                         ▼
                  Purchase History
```

## Technical Stack

- **Framework**: Spring Boot 3.2.2
- **Language**: Java 17
- **Database**: H2 (In-Memory SQL Database)
- **Security**: Spring Security with API Key Authentication
- **Documentation**: OpenAPI 3 (Swagger)
- **Containerization**: Docker & Docker Compose
- **Testing**: JUnit 5, MockMvc, Testcontainers
- **Build Tool**: Maven
- **API Standard**: JSON API (https://jsonapi.org/)

## Services Description

### 1. Products Service (Port 8080)

**Responsibilities:**
- Product creation and management
- Product information retrieval
- Product catalog maintenance

**Endpoints:**
- `POST /api/products` - Create a new product
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products` - List all products

**Model:**
```java
Product {
    Long id;
    String name;
    BigDecimal price;
    String description; // optional
}
```

### 2. Inventory Service (Port 8081)

**Responsibilities:**
- Inventory quantity management
- Purchase processing (chosen location)
- Purchase history tracking
- Inter-service communication with Products Service

**Endpoints:**
- `GET /api/inventory/products/{productId}` - Get inventory by product ID
- `PUT /api/inventory/products/{productId}` - Update inventory quantity
- `POST /api/purchases` - Process purchase (main endpoint)

**Models:**
```java
Inventory {
    Long id;
    Long productId;
    Integer quantity;
}

Purchase {
    Long id;
    Long productId;
    Integer quantity;
    BigDecimal totalPrice;
    LocalDateTime purchaseDate;
}
```

## Purchase Flow Design

### Decision: Purchase Endpoint Location

**Chosen Location: Inventory Service**

**Justification:**
1. **Single Responsibility**: Inventory service owns stock management
2. **Data Consistency**: Purchase and inventory updates happen in the same transaction
3. **Reduced Coupling**: Products service remains focused on product management
4. **Business Logic Alignment**: Purchase is fundamentally an inventory operation

### Purchase Process Flow

```
1. Client sends purchase request
   ↓
2. Inventory Service validates product existence (calls Products Service)
   ↓
3. Check inventory availability
   ↓
4. If sufficient stock:
   a) Decrease inventory quantity
   b) Calculate total price
   c) Create purchase record
   d) Return purchase confirmation
   ↓
5. If insufficient stock or product not found:
   Return appropriate error response
```

### Error Handling

- **Product Not Found (404)**: When product doesn't exist in Products Service
- **Insufficient Inventory (400)**: When requested quantity exceeds available stock
- **Validation Errors (400)**: Invalid input data
- **Service Communication Errors (500)**: Products Service unavailable

## Installation & Setup

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd spring-microservices
   ```

2. **Build the services**
   ```bash
   # Products Service
   cd products-service
   mvn clean package
   cd ..
   
   # Inventory Service
   cd inventory-service
   mvn clean package
   cd ..
   ```

3. **Run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

4. **Verify services are running**
   - Products Service: http://localhost:8080/actuator/health
   - Inventory Service: http://localhost:8081/actuator/health

### Manual Setup (Development)

1. **Start Products Service**
   ```bash
   cd products-service
   mvn spring-boot:run
   ```

2. **Start Inventory Service**
   ```bash
   cd inventory-service
   mvn spring-boot:run
   ```

## API Documentation

### OpenAPI/Swagger Documentation

Once services are running, access the interactive API documentation:

- **Products Service**: http://localhost:8080/swagger-ui/index.html
- **Inventory Service**: http://localhost:8081/swagger-ui/index.html

### Authentication

All API endpoints require API Key authentication via `X-API-Key` header:

- **Products Service**: `products-service-api-key-123`
- **Inventory Service**: `inventory-service-api-key-456`

### Sample API Calls

#### Create a Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "X-API-Key: products-service-api-key-123" \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "type": "product",
      "attributes": {
        "name": "Gaming Laptop",
        "price": 1299.99,
        "description": "High-performance gaming laptop"
      }
    }
  }'
```

#### Update Inventory
```bash
curl -X PUT http://localhost:8081/api/inventory/products/1 \
  -H "X-API-Key: inventory-service-api-key-456" \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "type": "inventory",
      "attributes": {
        "productId": 1,
        "quantity": 50
      }
    }
  }'
```

#### Process Purchase
```bash
curl -X POST http://localhost:8081/api/purchases \
  -H "X-API-Key: inventory-service-api-key-456" \
  -H "Content-Type: application/json" \
  -d '{
    "data": {
      "type": "purchase-request",
      "attributes": {
        "productId": 1,
        "quantity": 2
      }
    }
  }'
```

## Testing

### Running Tests

```bash
# Products Service Tests
cd products-service
mvn test

# Inventory Service Tests
cd inventory-service
mvn test

# Run all tests with coverage
mvn test jacoco:report
```

### Test Coverage

The project includes comprehensive testing:

- **Unit Tests**: Service layer logic, business rules
- **Integration Tests**: End-to-end API testing
- **Controller Tests**: HTTP layer testing with MockMvc
- **Coverage Target**: ≥ 80%

### Test Categories

1. **Product Management Tests**
   - Product creation validation
   - Product retrieval functionality
   - Error handling scenarios

2. **Inventory Management Tests**
   - Inventory updates and queries
   - Stock availability checks
   - Cross-service communication

3. **Purchase Flow Tests**
   - Complete purchase processing
   - Insufficient inventory handling
   - Product not found scenarios
   - Transaction rollback testing

## Monitoring & Health Checks

### Health Endpoints

- **Products Service**: http://localhost:8080/actuator/health
- **Inventory Service**: http://localhost:8081/actuator/health

### Metrics & Monitoring

- **Actuator Endpoints**: `/actuator/metrics`, `/actuator/info`
- **Structured Logging**: JSON formatted logs with correlation IDs
- **Custom Health Indicators**: Service-specific health checks

### Logging Configuration

- **Development**: Console output with DEBUG level
- **Production**: File-based logging with rotation
- **Format**: Structured logging with timestamps and service context

## Technical Decisions

### 1. Database Choice: H2 SQL Database

**Rationale:**
- **Structured Data**: Clear relational model (Products ↔ Inventory ↔ Purchases)
- **ACID Compliance**: Essential for purchase transactions
- **Development Simplicity**: Zero configuration, easy testing
- **Production Ready**: Easy migration to PostgreSQL/MySQL

### 2. Purchase Endpoint Location: Inventory Service

**Rationale:**
- **Business Logic Alignment**: Purchase is fundamentally an inventory operation
- **Data Consistency**: Single transaction for inventory update and purchase creation
- **Service Boundaries**: Maintains clear service responsibilities
- **Reduced Coupling**: Products service stays focused on product management

### 3. JSON API Standard Implementation

**Rationale:**
- **Standardization**: Industry-standard API format
- **Consistency**: Uniform response structure across all endpoints
- **Error Handling**: Structured error responses
- **Future-Proof**: Easy integration with frontend frameworks

### 4. API Key Authentication

**Rationale:**
- **Simplicity**: Easy to implement and test
- **Service-to-Service**: Appropriate for microservice communication
- **Stateless**: No session management required
- **Scalable**: Easy to extend with more sophisticated auth later

### 5. Timeout and Retry Strategy

**Implementation:**
- **HTTP Client Timeouts**: 10-second timeout for service calls
- **Retry Logic**: 3 retry attempts with exponential backoff
- **Circuit Breaker Pattern**: Ready for implementation if needed

## AI Tools Usage

### Tools Utilized

1. **Claude Code (Primary Assistant)**
   - **Architecture Design**: Microservice structure and communication patterns
   - **Code Generation**: Complete service implementations following best practices
   - **Test Creation**: Comprehensive unit and integration test suites
   - **Configuration**: Docker, logging, and monitoring setup

2. **Code Quality Verification Methods**

   **Static Analysis:**
   - Code review for Spring Boot best practices
   - Design pattern implementation verification
   - Security vulnerability assessment

   **Testing Verification:**
   - Unit test coverage analysis
   - Integration test scenario validation
   - Mock implementation correctness

   **Architectural Review:**
   - Service boundary validation
   - API design consistency check
   - Database design normalization

### Specific AI Contributions

1. **Service Implementation**
   - Generated complete REST controllers with JSON API compliance
   - Implemented comprehensive error handling
   - Created robust service layer with transaction management

2. **Testing Strategy**
   - Generated unit tests covering all business logic scenarios
   - Created integration tests for end-to-end workflows
   - Implemented test data builders and fixtures

3. **Configuration & DevOps**
   - Docker containerization setup
   - Logging configuration with environment-specific profiles
   - Health check implementation

4. **Documentation**
   - OpenAPI specification generation
   - Architecture diagram creation
   - Comprehensive README documentation

### Quality Assurance Process

1. **Code Review**: Manual review of all generated code for best practices
2. **Test Execution**: All generated tests executed and validated
3. **Integration Testing**: Manual testing of service interactions
4. **Performance Validation**: Basic load testing of critical endpoints
5. **Security Review**: API authentication and authorization validation

---

## Getting Started Quick Guide

1. **Start Services**: `docker-compose up --build`
2. **Create Product**: Use Swagger UI at http://localhost:8080/swagger-ui/index.html
3. **Add Inventory**: Update inventory via http://localhost:8081/swagger-ui/index.html  
4. **Process Purchase**: Make purchase through inventory service
5. **Monitor Health**: Check http://localhost:8080/actuator/health and http://localhost:8081/actuator/health

For detailed API examples and advanced configuration, refer to the sections above.