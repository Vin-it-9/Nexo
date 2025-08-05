# Nexo - Multi-Framework Cryptocurrency Platform

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green.svg)
![Micronaut](https://img.shields.io/badge/Micronaut-4.0-purple.svg)
![Quarkus](https://img.shields.io/badge/Quarkus-3.0-orange.svg)

A modern cryptocurrency platform built with a polyglot microservices architecture, leveraging multiple frameworks (Spring Boot, Micronaut, Quarkus) for optimized performance. Nexo enables users to manage wallets, track cryptocurrency prices in real-time, and perform trading operations through a unified gateway.

## Architecture

The platform implements a multi-framework microservices architecture:

```
┌───────────────┐     ┌───────────────┐
│ Frontend      │────▶│ API Gateway   │
│ Server        │◀────│ (Spring Boot) │
└───────────────┘     └───────┬───────┘
                              │
                      ┌───────▼───────┐
                      │ Discovery     │
                      │ Server        │
                      └───────┬───────┘
                              │
        ┌──────────────┬──────┴─────────┬──────────────┐
        │              │                │              │
┌───────▼──────┐ ┌─────▼─────┐   ┌──────▼───────┐ ┌────▼────────┐
│ User Service  │ │ Wallet    │   │ PriceFeed    │ │ Other       │
│ (Spring Boot) │ │ Service   │   │ Service      │ │ Services    │
└───────────────┘ │(Micronaut)│   │ (Quarkus)    │ └─────────────┘
                  └───────────┘   └──────────────┘
```

## Services

### 1. API Gateway (Spring Boot)
- Central entry point for all client requests
- Routes requests to appropriate microservices
- Implements JWT validation and authorization
- Provides load balancing for services
- Implements rate limiting for API protection
- Circuit breaking for service resilience

### 2. Discovery Server (Spring Boot)
- Service registry for dynamic service discovery
- Health monitoring for all registered services
- Load balancing support

### 3. User Service (Spring Boot)
- User registration and authentication
- OAuth 2.0 integration with Google
- JWT token generation and validation
- Role-based access control
- Cross-service authentication provider
- Swagger UI for API documentation

### 4. Wallet Service (Micronaut)
- Cryptocurrency wallet management
- Support for multiple wallets per user
- CRUD operations for wallets
- Transaction history tracking
- Balance management
- Leverages Micronaut's lightweight runtime
- Cross-service authentication with User Service

### 5. PriceFeed Service (Quarkus)
- Real-time cryptocurrency price data
- Integration with CoinMarketCap API
- In-memory caching for optimized performance
- Rate limiting for external API calls
- Fast startup with Quarkus native compilation

### 6. Frontend Server (Spring Boot)
- Web interface for user interaction
- Responsive design for desktop and mobile
- Real-time data visualization

## Technology Stack

### Cross-Cutting Concerns
- **Security**: JWT for authentication across services
- **Documentation**: Swagger UI for all service APIs
- **Resilience**: Rate limiting, circuit breakers, load balancing
- **Caching**: In-memory caching for performance optimization

### Framework-Specific
- **Spring Boot**: Full-featured services with comprehensive ecosystem
- **Micronaut**: Low memory footprint, fast startup time
- **Quarkus**: Native compilation, supersonic startup

### External Services
- **CoinMarketCap API**: Real-time cryptocurrency data

## Getting Started

### Prerequisites
- Java 17+
- Maven and/or Gradle
- Docker and Docker Compose
- CoinMarketCap API key

## Security Implementation

- **Authentication**: OAuth 2.0 with Google, JWT tokens
- **Cross-Service Security**: Shared JWT validation
- **API Protection**: Rate limiting, request validation

## Service Configuration

### API Gateway (Spring Boot)
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**,/api/auth/**
          filters:
            - name: CircuitBreaker
              args:
                name: userServiceCircuitBreaker
```

### Wallet Service (Micronaut)
```yaml
micronaut:
  application:
    name: wallet-service
  security:
    enabled: true
    token:
      jwt:
        enabled: true
```

### PriceFeed Service (Quarkus)
```properties
quarkus.application.name=pricefeed-service
quarkus.http.port=8092
quarkus.cache.caffeine."prices".maximum-size=100
quarkus.cache.caffeine."prices".expire-after-write=10M
```

##  Features by Service

### User Service
- Registration and login
- OAuth with Google
- Password reset
- Profile management
- Role management
- JWT token issuance
- Cross-service authentication

### Wallet Service
- Create multiple wallets per user
- Support for different cryptocurrencies
- Wallet balance tracking
- Transaction history
- Deposit/withdrawal operations

### PriceFeed Service
- Real-time cryptocurrency prices
- Historical price data
- Price alerts
- Market trends
- Optimized with in-memory caching
