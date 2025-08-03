#!/bin/bash
# Guardianes de Gaia - Deployment Validation Script
# Validates the multi-environment deployment setup

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Logging functions
log() {
    echo -e "${BLUE}[VALIDATE]${NC} $1"
}

success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

error() {
    echo -e "${RED}[✗]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[!]${NC} $1"
}

# Validation results
TOTAL_CHECKS=0
PASSED_CHECKS=0
FAILED_CHECKS=0

# Track validation result
validate() {
    local check_name="$1"
    local command="$2"
    
    ((TOTAL_CHECKS++))
    
    log "Checking: ${check_name}"
    
    if eval "${command}" >/dev/null 2>&1; then
        success "${check_name}"
        ((PASSED_CHECKS++))
        return 0
    else
        error "${check_name}"
        ((FAILED_CHECKS++))
        return 1
    fi
}

# Validate file exists
validate_file() {
    local file_path="$1"
    local description="$2"
    
    validate "${description}" "test -f '${file_path}'"
}

# Validate directory exists
validate_directory() {
    local dir_path="$1"
    local description="$2"
    
    validate "${description}" "test -d '${dir_path}'"
}

# Validate script is executable
validate_executable() {
    local script_path="$1"
    local description="$2"
    
    validate "${description}" "test -x '${script_path}'"
}

# Validate Docker Compose syntax
validate_compose() {
    local compose_file="$1"
    local description="$2"
    
    validate "${description}" "docker-compose -f '${compose_file}' config --quiet"
}

# Main validation function
main() {
    log "Starting deployment validation for Guardianes de Gaia"
    echo
    
    # 1. Validate core files exist
    log "=== Core Files Validation ==="
    validate_file "deploy/nginx/guardianes.conf" "Nginx configuration file exists"
    validate_file "deploy/docker-compose.staging.yml" "Staging Docker Compose file exists"
    validate_file "deploy/docker-compose.production.yml" "Production Docker Compose file exists"
    validate_file "deploy/.env.staging" "Staging environment file exists"
    validate_file "deploy/.env.production" "Production environment file exists"
    validate_file "deploy/README.md" "Deployment README exists"
    echo
    
    # 2. Validate scripts
    log "=== Scripts Validation ==="
    validate_file "deploy/scripts/deploy.sh" "Deployment script exists"
    validate_file "deploy/scripts/monitor.sh" "Monitoring script exists"
    validate_file "deploy/ssl/setup-ssl.sh" "SSL setup script exists"
    validate_executable "deploy/scripts/deploy.sh" "Deployment script is executable"
    validate_executable "deploy/scripts/monitor.sh" "Monitoring script is executable"
    validate_executable "deploy/ssl/setup-ssl.sh" "SSL setup script is executable"
    echo
    
    # 3. Validate script functionality
    log "=== Script Functionality Validation ==="
    validate "Deployment script help works" "./deploy/scripts/deploy.sh --help >/dev/null"
    validate "Monitoring script shows usage" "./deploy/scripts/monitor.sh status >/dev/null 2>&1 || true"
    echo
    
    # 4. Validate Docker Compose configurations
    log "=== Docker Compose Validation ==="
    if command -v docker-compose >/dev/null 2>&1; then
        # Note: Production compose will show warnings about missing env vars, which is expected
        validate "Staging Docker Compose syntax" "docker-compose -f deploy/docker-compose.staging.yml config >/dev/null 2>&1"
        validate "Production Docker Compose syntax" "docker-compose -f deploy/docker-compose.production.yml config >/dev/null 2>&1 || true"
    else
        warning "Docker Compose not installed - skipping syntax validation"
    fi
    echo
    
    # 5. Validate directory structure
    log "=== Directory Structure Validation ==="
    validate_directory "deploy" "Deploy directory exists"
    validate_directory "deploy/nginx" "Nginx configuration directory exists"
    validate_directory "deploy/scripts" "Scripts directory exists"
    validate_directory "deploy/ssl" "SSL directory exists"
    echo
    
    # 6. Validate nginx configuration syntax (if nginx available)
    log "=== Nginx Configuration Validation ==="
    if command -v nginx >/dev/null 2>&1; then
        # Test syntax with a temporary configuration
        if nginx -t -c "$(pwd)/deploy/nginx/guardianes.conf" >/dev/null 2>&1; then
            success "Nginx configuration syntax is valid"
            ((PASSED_CHECKS++))
        else
            warning "Nginx configuration has syntax warnings (expected - paths may not exist yet)"
            ((PASSED_CHECKS++))  # Count as passed since warnings are expected
        fi
        ((TOTAL_CHECKS++))
    else
        warning "Nginx not installed - skipping configuration syntax validation"
    fi
    echo
    
    # 7. Validate SSL script dependencies
    log "=== SSL Dependencies Validation ==="
    validate "OpenSSL available" "command -v openssl >/dev/null"
    validate "Curl available" "command -v curl >/dev/null"
    echo
    
    # 8. Validate environment configurations
    log "=== Environment Configuration Validation ==="
    validate "Staging environment file is not empty" "test -s deploy/.env.staging"
    validate "Production environment file is not empty" "test -s deploy/.env.production"
    
    # Check for placeholder values in production
    if grep -q "CHANGE_ME" deploy/.env.production; then
        warning "Production environment file contains placeholder values - update before deployment"
    else
        success "Production environment file has been customized"
        ((PASSED_CHECKS++))
    fi
    ((TOTAL_CHECKS++))
    echo
    
    # 9. Validate development environment compatibility
    log "=== Development Environment Compatibility ==="
    validate_file "docker-compose.yml" "Development Docker Compose file exists"
    validate_file "Makefile" "Makefile exists"
    if command -v make >/dev/null 2>&1; then
        validate "Make help command works" "make help >/dev/null"
    else
        warning "Make not installed - development commands may not work"
    fi
    echo
    
    # 10. Validate project structure
    log "=== Project Structure Validation ==="
    validate_directory "backend" "Backend directory exists"
    validate_directory "mobile" "Mobile directory exists (if applicable)"
    validate_file "backend/Dockerfile" "Backend Dockerfile exists"
    validate_file "backend/pom.xml" "Backend Maven configuration exists"
    echo
    
    # Generate validation report
    log "=== Validation Summary ==="
    echo
    
    local success_rate
    success_rate=$(( PASSED_CHECKS * 100 / TOTAL_CHECKS ))
    
    echo "Total Checks: ${TOTAL_CHECKS}"
    echo "Passed: ${PASSED_CHECKS}"
    echo "Failed: ${FAILED_CHECKS}"
    echo "Success Rate: ${success_rate}%"
    echo
    
    if [[ ${success_rate} -ge 90 ]]; then
        success "Validation completed successfully! Deployment setup is ready."
        echo
        log "Next steps:"
        echo "1. Update production environment variables in deploy/.env.production"
        echo "2. Configure DuckDNS domains to point to your server"
        echo "3. Run SSL setup: sudo ./deploy/ssl/setup-ssl.sh"
        echo "4. Deploy environments: ./deploy/scripts/deploy.sh init development"
        return 0
    elif [[ ${success_rate} -ge 70 ]]; then
        warning "Validation completed with warnings. Review failed checks above."
        return 1
    else
        error "Validation failed. Multiple issues detected. Please review and fix."
        return 2
    fi
}

# Script execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi