#!/bin/sh
# ============================================
# Guardianes de Gaia - Development Entrypoint
# ============================================

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "${BLUE}================================================${NC}"
echo "${BLUE}🎮 Guardianes de Gaia - Backend Development${NC}"
echo "${BLUE}================================================${NC}"

# ============================================
# Función para esperar servicios
# ============================================
wait_for_service() {
    local host=$1
    local port=$2
    local service=$3
    
    echo "${YELLOW}⏳ Esperando a ${service}...${NC}"
    
    while ! nc -z $host $port; do
        sleep 1
    done
    
    echo "${GREEN}✅ ${service} está listo!${NC}"
}

# ============================================
# Esperar a los servicios necesarios
# ============================================
wait_for_service mysql 3306 "MySQL"
wait_for_service redis 6379 "Redis"
wait_for_service rabbitmq 5672 "RabbitMQ"

echo ""
echo "${YELLOW}🔧 Configurando ambiente de desarrollo...${NC}"

# ============================================
# Verificar si es primera ejecución
# ============================================
if [ ! -f "/app/.initialized" ]; then
    echo "${YELLOW}📦 Primera ejecución detectada${NC}"
    
    # Instalar dependencias
    echo "${YELLOW}📦 Instalando dependencias Maven...${NC}"
    mvn dependency:go-offline -B
    
    # Marcar como inicializado
    touch /app/.initialized
    
    echo "${GREEN}✅ Inicialización completa${NC}"
fi

# ============================================
# Ejecutar migraciones si existen
# ============================================
if [ -d "/app/src/main/resources/db/migration" ]; then
    echo "${YELLOW}🔄 Ejecutando migraciones de base de datos...${NC}"
    mvn flyway:migrate || {
        echo "${RED}❌ Error en migraciones${NC}"
        exit 1
    }
    echo "${GREEN}✅ Migraciones completadas${NC}"
fi

# ============================================
# Configurar hot-reload
# ============================================
export MAVEN_OPTS="-Dspring-boot.run.fork=false"

echo ""
echo "${GREEN}================================================${NC}"
echo "${GREEN}🚀 Iniciando aplicación en modo desarrollo${NC}"
echo "${GREEN}================================================${NC}"
echo ""
echo "📋 URLs disponibles:"
echo "  - API: ${BLUE}http://localhost:8080${NC}"
echo "  - Debug: ${BLUE}localhost:5005${NC}"
echo "  - Metrics: ${BLUE}http://localhost:9090/metrics${NC}"
echo "  - Health: ${BLUE}http://localhost:8080/actuator/health${NC}"
echo "  - Togglz: ${BLUE}http://localhost:8080/admin/toggles${NC}"
echo ""
echo "${YELLOW}👀 Watching for changes...${NC}"
echo ""

# ============================================
# Iniciar aplicación con hot-reload
# ============================================
exec "$@"