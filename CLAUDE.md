# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Guardianes de Gaia** is a cooperative card game mobile app that gamifies walking to school for families with children aged 6-12. The system converts daily walking steps into energy for card battles, promoting physical activity and family bonding.

## Development Commands

### Core Development
```bash
# Start full development environment
make up

# Stop all services
make down

# View all available commands
make help

# View logs (all services)
make logs

# View backend logs only
make logs-backend
```

### Backend Development
```bash
# Run all tests
make test

# Run integration tests
make test-integration

# Run Cucumber BDD tests
make test-cucumber

# Watch mode for tests
make test-watch

# Generate coverage report
make coverage
```

### Mobile Development
```bash
# Run Flutter app
make mobile-run

# Run mobile tests
make mobile-test

# Build APK
make mobile-build-apk
```

### Code Quality
```bash
# Format code (both backend and mobile)
make format

# Run linters
make lint

# Install dependencies
make install
```

### Security Validation
```bash
# Run security validation
make security-check

# Test secure deployment
make security-test

# Complete security validation
make security-validate
```

### Database Operations
```bash
# Access MySQL console
make db-console

# Access Redis CLI
make redis-cli

# Create database backup
make db-backup
```

### Development Environment
```bash
# Clean everything and restart fresh
make reset

# Start dev environment with backend logs
make dev

# Open backend container shell
make backend-shell
```

## Architecture Overview

### Multi-Service Architecture
- **Backend**: Java 17 + Spring Boot 3.2 with Domain-Driven Design
- **Mobile**: Flutter 3.x with BLoC pattern and Clean Architecture
- **Database**: MySQL 8.0 primary + Redis cache
- **Message Queue**: RabbitMQ for event processing
- **Monitoring**: Prometheus + Grafana stack

### Domain Structure
Based on the ubiquitous language, the system is organized around these core domains:

1. **Walking Domain**: Tracks step counting, routes, and energy generation
2. **Battle Domain**: Manages card battles, challenges, and cooperative gameplay
3. **Card Domain**: Handles card collection, QR scanning, and deck management
4. **Guardian Domain**: Player profiles, progression, and family groups (Pactos)

### Key Business Concepts
- **Guardián**: Child player with unique profile
- **Pacto**: Family group of 2-6 Guardians playing together
- **Guía del Pacto**: Rotating adult facilitator role
- **Energía Vital**: Resource generated from walking (1 energy = 10 steps)
- **Esencia de Gaia**: Universal resource for challenges
- **Ruta Mágica**: Predefined walking routes with special bonus points

### Service Endpoints
- **Backend API**: http://localhost:8080
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus Metrics**: http://localhost:9091
- **RabbitMQ Management**: http://localhost:15672
- **Feature Toggles**: http://localhost:8080/admin/toggles

### Development Setup
The project uses Docker Compose for local development with hot reload enabled. The makefile provides convenient commands for common development tasks.

### Testing Strategy
- **Unit Tests**: JUnit 5 for backend, Flutter test for mobile
- **Integration Tests**: TestContainers for database integration
- **BDD Tests**: Cucumber for acceptance testing
- **E2E Tests**: Planned for critical user flows

### Feature Management
Uses Togglz for feature toggles, allowing safe deployment of incomplete features and A/B testing capabilities.

### Key Technical Decisions
- **Cooperative-only gameplay**: No PvP to foster family bonding
- **Universal resource system**: Single energy type to avoid location dependencies
- **Manual route mapping**: Parents create custom magical routes vs automated POI detection
- **QR primary with NFC premium**: Accessibility over cutting-edge tech
- **Gradual complexity**: "Three Eras" system for progressive feature introduction

## Development Context

This is an MVP targeting 8-week development cycle with focus on validating core hypothesis: "Can a card game make walking to school addictive?" The current implementation includes basic step tracking, QR card scanning, 48 base cards, 5 daily challenges, and XP progression (levels 1-10).

### MVP Exclusions (Future Versions)
- Weekly narrative sagas
- Class specialization system
- Advanced loot and crafting
- Multi-Pacto communities
- Route sharing between families
- Seasonal events

### Environment Variables
Key environment variables are defined in docker-compose.yml:
- Database credentials (DB_USER, DB_PASSWORD, DB_ROOT_PASSWORD)
- RabbitMQ credentials (RABBITMQ_USER, RABBITMQ_PASS)
- Grafana credentials (GRAFANA_USER, GRAFANA_PASSWORD)