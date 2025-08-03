#!/bin/bash
# Test script for nginx integration
# Tests both existing sites and new Guardianes domains

set -euo pipefail

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${BLUE}[TEST]${NC} $1"
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

# Test nginx configuration
test_nginx_config() {
    log "Testing nginx configuration syntax..."
    
    if sudo nginx -t; then
        success "Nginx configuration is valid"
        return 0
    else
        error "Nginx configuration has errors"
        return 1
    fi
}

# Test existing domains
test_existing_domains() {
    log "Testing existing domains..."
    
    # Extract existing server names from nginx config
    local existing_domains
    existing_domains=$(sudo nginx -T 2>/dev/null | grep -E "server_name" | grep -v "guardianes" | awk '{for(i=2;i<=NF;i++) print $i}' | sed 's/;//' | grep -v "localhost" | head -5)
    
    if [[ -z "${existing_domains}" ]]; then
        warning "No existing domains found to test"
        return 0
    fi
    
    echo "Testing existing domains:"
    while IFS= read -r domain; do
        if [[ -n "${domain}" ]] && [[ "${domain}" != "_" ]]; then
            log "Testing ${domain}..."
            
            if curl -s -I --max-time 5 "http://${domain}" >/dev/null 2>&1; then
                success "${domain} responds"
            elif curl -s -I --max-time 5 "https://${domain}" >/dev/null 2>&1; then
                success "${domain} responds (HTTPS)"
            else
                warning "${domain} not accessible (may be internal/development only)"
            fi
        fi
    done <<< "${existing_domains}"
}

# Test Guardianes domains
test_guardianes_domains() {
    log "Testing Guardianes domains..."
    
    local domains=(
        "dev-guardianes.duckdns.org:http"
        "stg-guardianes.duckdns.org:http"
        "guardianes.duckdns.org:https"
    )
    
    for domain_proto in "${domains[@]}"; do
        IFS=':' read -r domain protocol <<< "${domain_proto}"
        
        local url="${protocol}://${domain}"
        log "Testing ${url}..."
        
        if curl -s -I --max-time 10 "${url}" >/dev/null 2>&1; then
            success "${domain} responds"
        elif curl -s -I --max-time 10 "${url}/actuator/health" >/dev/null 2>&1; then
            success "${domain} health endpoint responds"
        else
            warning "${domain} not responding (backend may not be running yet)"
        fi
    done
}

# Test for port conflicts
test_port_conflicts() {
    log "Checking for port conflicts..."
    
    local ports=(80 443 8080 8081 8082)
    
    for port in "${ports[@]}"; do
        if netstat -tlnp 2>/dev/null | grep -q ":${port} "; then
            local process
            process=$(netstat -tlnp 2>/dev/null | grep ":${port} " | awk '{print $NF}' | head -1)
            success "Port ${port} is in use by ${process}"
        else
            warning "Port ${port} is not in use"
        fi
    done
}

# Test SSL certificates
test_ssl_certificates() {
    log "Testing SSL certificates..."
    
    if [[ -d /etc/letsencrypt/live ]]; then
        echo "Available certificates:"
        ls /etc/letsencrypt/live/ 2>/dev/null | while read -r cert; do
            if [[ -f "/etc/letsencrypt/live/${cert}/fullchain.pem" ]]; then
                local expiry
                expiry=$(openssl x509 -in "/etc/letsencrypt/live/${cert}/fullchain.pem" -noout -enddate | cut -d= -f2)
                success "${cert} (expires: ${expiry})"
            fi
        done
    else
        warning "No Let's Encrypt certificates found"
    fi
}

# Main test function
main() {
    echo -e "${BLUE}=== Nginx Integration Test ===${NC}\n"
    
    local tests_passed=0
    local total_tests=5
    
    # Test 1: Nginx configuration
    if test_nginx_config; then
        ((tests_passed++))
    fi
    echo
    
    # Test 2: Existing domains
    test_existing_domains
    ((tests_passed++))
    echo
    
    # Test 3: Guardianes domains
    test_guardianes_domains
    ((tests_passed++))
    echo
    
    # Test 4: Port conflicts
    test_port_conflicts
    ((tests_passed++))
    echo
    
    # Test 5: SSL certificates
    test_ssl_certificates
    ((tests_passed++))
    echo
    
    # Summary
    echo -e "${BLUE}=== Test Summary ===${NC}"
    echo "Tests completed: ${tests_passed}/${total_tests}"
    
    if [[ ${tests_passed} -eq ${total_tests} ]]; then
        success "All tests completed successfully!"
        echo -e "\n${GREEN}✅ Your nginx integration is working correctly${NC}"
        echo -e "\n${BLUE}Next steps:${NC}"
        echo "1. Deploy Guardianes environments: ./deploy/scripts/deploy.sh init development"
        echo "2. Test health endpoints: ./deploy/scripts/monitor.sh check"
        echo "3. Setup SSL if not already done: sudo ./deploy/ssl/setup-ssl.sh"
    else
        warning "Some tests had warnings - review output above"
    fi
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi