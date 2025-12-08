# 🔧 Guardianes Backend

Java 17 + Spring Boot 3.2 backend service for Guardianes de Gaia.

## 🏗️ Architecture

- **Pattern**: Domain-Driven Design (DDD)
- **Framework**: Spring Boot 3.2
- **Database**: MySQL 8.0 + Redis cache
- **Messaging**: RabbitMQ

### Domain Structure

```
src/main/java/com/guardianes/
├── guardian/          # Guardian profile management
├── walking/           # Step tracking & energy generation
├── cards/             # Card collection & deck management
├── battle/            # Battle mechanics & challenges
└── shared/            # Cross-domain utilities
```

### Layer Structure (per domain)

```
domain/
├── application/       # Use cases and application services
├── domain/
│   ├── model/         # Aggregates, Entities, Value Objects
│   ├── events/        # Domain events
│   └── repository/    # Repository interfaces
└── infrastructure/
    ├── persistence/   # JPA repositories
    ├── web/           # REST controllers
    └── config/        # Spring configuration
```

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Docker & Docker Compose
- Maven 3.8+

### Running Locally

```bash
# Start dependencies (MySQL, Redis, RabbitMQ)
make up

# Run the backend
cd backend
mvn spring-boot:run
```

### API Endpoints

- **Health**: `http://localhost:8080/actuator/health`
- **API Docs**: `http://localhost:8080/swagger-ui.html`
- **Feature Toggles**: `http://localhost:8080/admin/toggles`

## 🧪 Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Cucumber BDD tests
mvn test -Pcucumber

# All tests
make test-all
```

## 📊 Monitoring

- **Prometheus**: `http://localhost:9091`
- **Grafana**: `http://localhost:3000`

## 📚 Related Documentation

- [Tech Stack](../docs/TECH_STACK.md)
- [Ubiquitous Language](../docs/UBIQUITOUS_LANGUAGE.md)
- [CD Pipeline](../docs/CD_PIPELINE_ANALYSIS.md)
