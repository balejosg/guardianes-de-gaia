#!/bin/bash
# =============================================================================
# SECURITY VALIDATION SCRIPT
# =============================================================================
# 
# This script validates that all security vulnerabilities have been fixed
# and that no credentials are exposed in the codebase.
#
# Usage: ./scripts/security_validation.sh
#
# =============================================================================

set -euo pipefail

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
VULNERABILITIES_FOUND=0
CHECKS_PASSED=0
TOTAL_CHECKS=0

# Helper functions
log_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

log_success() {
    echo -e "${GREEN}✅ $1${NC}"
    ((CHECKS_PASSED++))
}

log_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}❌ $1${NC}"
    ((VULNERABILITIES_FOUND++))
}

check_file() {
    local file="$1"
    local pattern="$2"
    local description="$3"
    
    ((TOTAL_CHECKS++))
    
    if [[ -f "$file" ]]; then
        if grep -q "$pattern" "$file"; then
            log_error "VULNERABILITY: $description in $file"
            echo "    Found: $(grep "$pattern" "$file" | head -1)"
        else
            log_success "$description check passed in $file"
        fi
    else
        log_warning "File not found: $file (skipping check)"
    fi
}

echo "=============================================================================="
echo "🔐 GUARDIANES DE GAIA - SECURITY VALIDATION"
echo "=============================================================================="
echo

log_info "Starting comprehensive security validation..."
echo

# =============================================================================
# 1. CHECK FOR HARDCODED CREDENTIALS
# =============================================================================
echo "📋 CHECKING FOR HARDCODED CREDENTIALS"
echo "----------------------------------------------------------------------"

# Check docker-compose.yml for default credentials
check_file "docker-compose.yml" ":-secret" "Default database password removal"
check_file "docker-compose.yml" ":-rootsecret" "Default root password removal"
check_file "docker-compose.yml" ":-admin" "Default Grafana password removal"
check_file "docker-compose.yml" "-p.*secret" "Exposed password in health check"

# Check application properties for hardcoded secrets
check_file "backend/src/main/resources/application-dev.properties" "secret.*=" "Hardcoded JWT secret removal"
check_file "backend/src/main/resources/application-dev.properties" "admin123" "Hardcoded admin password removal"
check_file "backend/src/main/resources/application-prod.properties" "secret.*=" "Production hardcoded secrets"

# Check makefile for hardcoded passwords
check_file "makefile" "-prootsecret" "Makefile hardcoded password removal"
check_file "makefile" "-padmin" "Makefile admin password removal"

# Check prometheus for hardcoded auth
check_file "docker/prometheus/prometheus.yml" "admin123" "Prometheus hardcoded auth removal"

echo

# =============================================================================
# 2. CHECK ENVIRONMENT VARIABLE USAGE
# =============================================================================
echo "🔧 VALIDATING ENVIRONMENT VARIABLE USAGE"
echo "----------------------------------------------------------------------"

((TOTAL_CHECKS++))
if grep -q '\${DB_PASSWORD}' docker-compose.yml; then
    log_success "Database password externalized to environment variable"
else
    log_error "Database password not properly externalized"
fi

((TOTAL_CHECKS++))
if grep -q '\${JWT_SECRET}' backend/src/main/resources/application-*.properties; then
    log_success "JWT secret externalized to environment variable"
else
    log_error "JWT secret not properly externalized"
fi

((TOTAL_CHECKS++))
if grep -q '\${GRAFANA_PASSWORD}' docker-compose.yml; then
    log_success "Grafana password externalized to environment variable"
else
    log_error "Grafana password not properly externalized"
fi

echo

# =============================================================================
# 3. CHECK SSL/TLS CONFIGURATION
# =============================================================================
echo "🔒 VALIDATING SSL/TLS CONFIGURATION"
echo "----------------------------------------------------------------------"

((TOTAL_CHECKS++))
if grep -q "useSSL=true" docker-compose.yml && grep -q "requireSSL=true" docker-compose.yml; then
    log_success "SSL enabled for database connections in docker-compose"
else
    log_error "SSL not properly enabled for database connections"
fi

((TOTAL_CHECKS++))
if grep -q "useSSL=true.*requireSSL=true" backend/src/main/resources/application-prod.properties; then
    log_success "SSL enabled for production database connections"
else
    log_error "SSL not enabled for production database connections"
fi

echo

# =============================================================================
# 4. CHECK SECURITY CONFIGURATIONS
# =============================================================================
echo "🛡️  VALIDATING SECURITY CONFIGURATIONS"
echo "----------------------------------------------------------------------"

((TOTAL_CHECKS++))
if grep -q "TOGGLZ_CONSOLE_SECURED=true" docker-compose.yml; then
    log_success "Feature toggle console secured"
else
    log_error "Feature toggle console not secured"
fi

((TOTAL_CHECKS++))
if grep -q "management.security.enabled=true" backend/src/main/resources/application-*.properties; then
    log_success "Actuator endpoints secured"
else
    log_error "Actuator endpoints not properly secured"
fi

((TOTAL_CHECKS++))
if ! grep -q "logging.level.*=DEBUG" backend/src/main/resources/application-dev.properties; then
    log_success "Debug logging removed from development configuration"
else
    log_error "Debug logging still enabled in development"
fi

echo

# =============================================================================
# 5. CHECK TEMPLATE FILES
# =============================================================================
echo "📄 VALIDATING TEMPLATE FILES"
echo "----------------------------------------------------------------------"

((TOTAL_CHECKS++))
if [[ -f ".env.template" ]]; then
    log_success "Environment template file exists"
else
    log_error "Environment template file missing"
fi

((TOTAL_CHECKS++))
if [[ -f ".env.prod.template" ]]; then
    log_success "Production environment template exists"
else
    log_error "Production environment template missing"
fi

((TOTAL_CHECKS++))
if [[ -f "scripts/generate_prometheus_config.sh" ]]; then
    log_success "Prometheus configuration generator exists"
else
    log_error "Prometheus configuration generator missing"
fi

echo

# =============================================================================
# 6. SUMMARY
# =============================================================================
echo "=============================================================================="
echo "📊 SECURITY VALIDATION SUMMARY"
echo "=============================================================================="
echo
echo "Total Checks: $TOTAL_CHECKS"
echo "Checks Passed: $CHECKS_PASSED"
echo "Vulnerabilities Found: $VULNERABILITIES_FOUND"
echo

if [[ $VULNERABILITIES_FOUND -eq 0 ]]; then
    log_success "🎉 ALL SECURITY CHECKS PASSED!"
    echo "   The application is ready for secure deployment."
    exit 0
else
    log_error "🚨 SECURITY VULNERABILITIES DETECTED!"
    echo "   Please fix the $VULNERABILITIES_FOUND vulnerabilities before deployment."
    echo
    echo "💡 NEXT STEPS:"
    echo "   1. Review and fix all vulnerabilities listed above"
    echo "   2. Run this script again to verify fixes"
    echo "   3. Ensure all environment variables are properly set"
    echo "   4. Test application functionality with secure configuration"
    exit 1
fi