# Guardianes de Gaia - Makefile
# Comandos útiles para desarrollo

.DEFAULT_GOAL := help
.PHONY: help build up down logs test clean

# Colores para output
GREEN  := $(shell tput -Txterm setaf 2)
YELLOW := $(shell tput -Txterm setaf 3)
WHITE  := $(shell tput -Txterm setaf 7)
CYAN   := $(shell tput -Txterm setaf 6)
RESET  := $(shell tput -Txterm sgr0)

## ============================================================================
## 🎯 COMANDOS PRINCIPALES
## ============================================================================

help: ## Muestra esta ayuda
	@echo ''
	@echo '${GREEN}Guardianes de Gaia${RESET} - ${YELLOW}Comandos disponibles${RESET}'
	@echo ''
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "${CYAN}%-20s${RESET} %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ''

## ============================================================================
## 🐳 DOCKER
## ============================================================================

build: ## Construye todos los contenedores
	@echo "${YELLOW}🔨 Construyendo contenedores...${RESET}"
	docker-compose build

up: ## Levanta todos los servicios
	@echo "${YELLOW}🚀 Iniciando servicios...${RESET}"
	docker-compose up -d
	@echo "${GREEN}✅ Servicios iniciados${RESET}"
	@echo ""
	@echo "📋 URLs disponibles:"
	@echo "  - Backend API: ${CYAN}http://localhost:8080${RESET}"
	@echo "  - Grafana: ${CYAN}http://localhost:3000${RESET} (admin/admin)"
	@echo "  - Prometheus: ${CYAN}http://localhost:9090${RESET}"
	@echo "  - Togglz: ${CYAN}http://localhost:8080/admin/toggles${RESET}"

down: ## Detiene todos los servicios
	@echo "${YELLOW}🛑 Deteniendo servicios...${RESET}"
	docker-compose down

restart: down up ## Reinicia todos los servicios

logs: ## Muestra logs de todos los servicios
	docker-compose logs -f

logs-backend: ## Muestra logs del backend
	docker-compose logs -f backend

ps: ## Lista los contenedores en ejecución
	docker-compose ps

## ============================================================================
## 🧪 TESTING
## ============================================================================

test: ## Ejecuta todos los tests
	@echo "${YELLOW}🧪 Ejecutando tests...${RESET}"
	cd backend && mvn test

test-integration: ## Ejecuta tests de integración
	@echo "${YELLOW}🧪 Ejecutando tests de integración...${RESET}"
	docker-compose -f docker-compose.test.yml up --abort-on-container-exit

test-cucumber: ## Ejecuta tests de Cucumber
	@echo "${YELLOW}🥒 Ejecutando tests de Cucumber...${RESET}"
	cd backend && mvn verify -Pcucumber

test-watch: ## Ejecuta tests en modo watch
	cd backend && mvn test -Dtest.watch=true

coverage: ## Genera reporte de cobertura
	@echo "${YELLOW}📊 Generando reporte de cobertura...${RESET}"
	cd backend && mvn jacoco:report
	@echo "${GREEN}✅ Reporte disponible en: backend/target/site/jacoco/index.html${RESET}"

## ============================================================================
## 💾 BASE DE DATOS
## ============================================================================

db-console: ## Abre consola MySQL
	docker-compose exec mysql mysql -u guardianes -p guardianes

db-backup: ## Backup de la base de datos
	@echo "${YELLOW}💾 Creando backup...${RESET}"
	docker-compose exec mysql mysqldump -u root -prootsecret guardianes > backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "${GREEN}✅ Backup creado${RESET}"

db-restore: ## Restaura la base de datos desde backup
	@echo "${YELLOW}💾 Restaurando backup...${RESET}"
	@read -p "Archivo de backup: " file; \
	docker-compose exec -T mysql mysql -u root -prootsecret guardianes < $$file

redis-cli: ## Abre Redis CLI
	docker-compose exec redis redis-cli

## ============================================================================
## 🚀 DESARROLLO
## ============================================================================

backend-shell: ## Abre shell en el contenedor backend
	docker-compose exec backend sh

backend-debug: ## Inicia backend en modo debug (puerto 5005)
	@echo "${YELLOW}🐛 Backend en modo debug - Puerto 5005${RESET}"
	docker-compose run --rm -p 5005:5005 backend

mobile-run: ## Ejecuta la app Flutter
	cd mobile && flutter run

mobile-build-apk: ## Construye APK de la app
	cd mobile && flutter build apk --release

mobile-test: ## Ejecuta tests de Flutter
	cd mobile && flutter test

## ============================================================================
## 🔧 UTILIDADES
## ============================================================================

clean: ## Limpia contenedores y volúmenes
	@echo "${YELLOW}🧹 Limpiando...${RESET}"
	docker-compose down -v
	docker system prune -f
	cd backend && mvn clean
	cd mobile && flutter clean

install: ## Instala dependencias
	@echo "${YELLOW}📦 Instalando dependencias...${RESET}"
	cd backend && mvn install -DskipTests
	cd mobile && flutter pub get

format: ## Formatea el código
	@echo "${YELLOW}✨ Formateando código...${RESET}"
	cd backend && mvn spotless:apply
	cd mobile && flutter format .

lint: ## Ejecuta linters
	@echo "${YELLOW}🔍 Analizando código...${RESET}"
	cd backend && mvn spotless:check
	cd mobile && flutter analyze

## ============================================================================
## 📊 MONITOREO
## ============================================================================

monitoring: ## Abre todas las URLs de monitoreo
	@echo "${YELLOW}📊 Abriendo dashboards...${RESET}"
	@echo "  - Grafana: ${CYAN}http://localhost:3000${RESET}"
	@echo "  - Prometheus: ${CYAN}http://localhost:9090${RESET}"
	@echo "  - Backend Health: ${CYAN}http://localhost:8080/actuator/health${RESET}"

metrics: ## Muestra métricas actuales
	curl -s http://localhost:8080/actuator/prometheus | grep -E "guardian|walking|battle" | head -20

## ============================================================================
## 📦 RELEASE
## ============================================================================

version: ## Muestra la versión actual
	@echo "${GREEN}Version actual: ${YELLOW}$$(cat VERSION)${RESET}"

release-patch: ## Crea release patch (x.x.+1)
	@echo "${YELLOW}📦 Creando release patch...${RESET}"
	./scripts/release.sh patch

release-minor: ## Crea release minor (x.+1.0)
	@echo "${YELLOW}📦 Creando release minor...${RESET}"
	./scripts/release.sh minor

release-major: ## Crea release major (+1.0.0)
	@echo "${YELLOW}📦 Creando release major...${RESET}"
	./scripts/release.sh major

## ============================================================================
## 🏃 ATAJOS
## ============================================================================

dev: up logs-backend ## Inicia entorno de desarrollo

stop: down ## Alias para 'down'

reset: clean up ## Limpia y reinicia todo

check: lint test ## Ejecuta linters y tests