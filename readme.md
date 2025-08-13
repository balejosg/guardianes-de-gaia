# 🎮 Guardianes de Gaia

<p align="center">
  <img src="docs/images/logo.png" alt="Guardianes de Gaia" width="200"/>
</p>

<p align="center">
  <strong>Transforma el camino al colegio en una aventura épica</strong>
</p>

<p align="center">
  <a href="#"><img src="https://img.shields.io/badge/version-0.1.0--MVP-blue.svg" alt="Version"></a>
  <a href="#"><img src="https://img.shields.io/badge/java-17-orange.svg" alt="Java"></a>
  <a href="#"><img src="https://img.shields.io/badge/flutter-3.x-blue.svg" alt="Flutter"></a>
  <a href="#"><img src="https://img.shields.io/badge/license-MIT-green.svg" alt="License"></a>
</p>

## 🌟 Descripción

Guardianes de Gaia es un juego móvil cooperativo que gamifica los trayectos a pie al colegio, convirtiendo cada paso en energía para batallas de cartas épicas. Diseñado para familias con niños de 6-12 años, fomenta el ejercicio físico diario mientras fortalece los vínculos familiares.

## 🎯 Características Principales

- **🚶 Tracking de Pasos**: Convierte pasos en energía para el juego
- **🎴 Sistema de Cartas**: 48 cartas coleccionables con códigos QR
- **⚔️ Batallas Cooperativas**: Desafíos diarios para completar en familia
- **🗺️ Rutas Mágicas**: Crea tus propias rutas con puntos de bonus
- **📊 Progresión**: Sistema de niveles y logros
- **👨‍👩‍👧‍👦 Modo Pacto**: Juega con hasta 6 miembros de la familia

## 🛠️ Stack Tecnológico

### Backend
- Java 17 + Spring Boot 3.2
- MySQL 8.0 + Redis
- Docker + Docker Compose
- Domain-Driven Design (DDD)

### Mobile
- Flutter 3.x
- BLoC Pattern
- Firebase Analytics
- Clean Architecture

### DevOps
- GitHub Actions
- Prometheus + Grafana
- TDD con JUnit 5
- BDD con Cucumber

## 🚀 Quick Start

### Prerequisitos
- Docker & Docker Compose
- Java 17+
- Flutter 3.x
- Make (opcional)

### Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/guardianes-de-gaia.git
cd guardianes-de-gaia
```

2. **Levantar servicios con Docker**
```bash
make up
# o sin Make:
docker-compose up -d
```

3. **Verificar servicios**
```bash
make logs
# Backend: http://localhost:8090 (dev), http://dev-guardianes.duckdns.org/api
# Grafana: http://localhost:3000 (admin/admin), http://dev-guardianes.duckdns.org/grafana
# Prometheus: http://localhost:9091, http://dev-guardianes.duckdns.org/prometheus
```

4. **Ejecutar tests**
```bash
make test
```

## 📱 Desarrollo Mobile

```bash
cd mobile
flutter pub get
flutter run
```

## 📖 Documentación

- [Documento del Proyecto](docs/PROYECTO.md)
- [Stack Tecnológico](docs/TECH_STACK.md)
- [Lenguaje Ubicuo](docs/UBIQUITOUS_LANGUAGE.md)
- [Estrategia de Monitoreo](docs/monitoring-strategy.md)

## 🧪 Testing

```bash
# Tests unitarios
mvn test

# Tests de integración
mvn verify

# Tests con Cucumber
mvn test -Pcucumber

# Tests en contenedores
docker-compose -f docker-compose.test.yml up
```

## 📊 Monitoreo

- **Métricas**: http://localhost:9091 (Prometheus), http://dev-guardianes.duckdns.org/prometheus
- **Dashboards**: http://localhost:3000 (Grafana), http://dev-guardianes.duckdns.org/grafana  
- **Feature Toggles**: http://localhost:8090/admin/toggles, http://dev-guardianes.duckdns.org/admin/toggles
- **Health Check**: http://localhost:8090/actuator/health, http://dev-guardianes.duckdns.org/actuator/health

## 🤝 Contribuir

1. Fork el proyecto
2. Crea tu feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add: nueva funcionalidad'`)
4. Push a la branch (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

### Convenciones de Commits
- `feat:` Nueva funcionalidad
- `fix:` Corrección de bugs
- `test:` Añadir tests
- `docs:` Cambios en documentación
- `refactor:` Refactorización de código

## 📅 Roadmap

### MVP (8 semanas) ✅
- [x] Sistema de tracking de pasos
- [x] 48 cartas básicas con QR
- [x] 5 desafíos diarios
- [x] Sistema de niveles (1-10)
- [ ] Beta testing con 20 familias

### v2.0 - Sagas Semanales
- [ ] Narrativas de 5 días conectadas
- [ ] Sistema de crafteo
- [ ] Eventos estacionales

### v3.0 - Comunidad
- [ ] Intercambio de rutas
- [ ] Guilds multi-familiares
- [ ] Torneos locales

## 👥 Equipo

- **Product Owner**: [Nombre]
- **Tech Lead**: [Nombre]
- **Desarrolladores**: [Nombres]

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE.md](LICENSE.md) para detalles.

## 🙏 Agradecimientos

- A todas las familias beta testers
- A la comunidad open source
- A Gaia, por inspirarnos a cuidar el planeta paso a paso

---

<p align="center">
  Hecho con ❤️ para familias activas
</p>