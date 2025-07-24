# Claude Code Memory

This file contains important information about the project for future development sessions.

## Project Overview
- Two Spring Boot microservices: Products Service (8080) and Inventory Service (8081)
- JSON API standard implementation
- H2 in-memory databases for both services
- API key authentication between services
- Docker containerization with docker-compose
- Comprehensive unit and integration tests
- OpenAPI/Swagger documentation

## Key Technical Decisions
1. **Purchase endpoint location**: Implemented in Inventory Service for better data consistency and service boundaries
2. **Database choice**: H2 SQL database for structured relational data and ACID compliance
3. **Authentication**: API key based authentication for simplicity and microservice communication
4. **Communication**: HTTP REST with timeout and retry logic

## API Keys
- Products Service: `products-service-api-key-123`
- Inventory Service: `inventory-service-api-key-456`

## Build Commands
```bash
# Build both services
mvn clean package

# Run tests
mvn test

# Start with Docker
docker-compose up --build
```

## Service URLs
- Products Service: http://localhost:8080
- Inventory Service: http://localhost:8081
- Swagger UIs available at /swagger-ui/index.html

## Test Coverage Target
≥ 80% test coverage with comprehensive unit and integration tests

## Git Flow
- Development branch: `development`
- Clean commit messages without co-author tags
- Git user: Jhons14 <tivenorjuela7@gmail.com>