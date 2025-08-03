#!/bin/bash
# Guardianes de Gaia - Health Monitoring Script
# Monitors all environments and sends alerts if issues are detected

# Check if we're running in bash
if [ -z "${BASH_VERSION:-}" ]; then
    echo "This script requires bash. Please run with: bash $0 $*"
    exit 1
fi

set -euo pipefail

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

# Environment configuration
ENVIRONMENTS=(
    "development:8080:dev-guardianes.duckdns.org"
    "staging:8081:stg-guardianes.duckdns.org"
    "production:8082:guardianes.duckdns.org"
)

# Monitoring settings
CHECK_INTERVAL=${CHECK_INTERVAL:-60}  # seconds
ALERT_THRESHOLD=${ALERT_THRESHOLD:-3}  # failed checks before alert
LOG_FILE="${PROJECT_DIR}/logs/monitoring.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Logging functions
log() {
    local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
    echo -e "${BLUE}[${timestamp}]${NC} $1" | tee -a "${LOG_FILE}"
}

error() {
    local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
    echo -e "${RED}[${timestamp}] ERROR:${NC} $1" | tee -a "${LOG_FILE}"
}

success() {
    local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
    echo -e "${GREEN}[${timestamp}] SUCCESS:${NC} $1" | tee -a "${LOG_FILE}"
}

warning() {
    local timestamp=$(date +'%Y-%m-%d %H:%M:%S')
    echo -e "${YELLOW}[${timestamp}] WARNING:${NC} $1" | tee -a "${LOG_FILE}"
}

# Initialize monitoring
init_monitoring() {
    log "Initializing health monitoring for Guardianes de Gaia"
    
    # Create logs directory
    mkdir -p "$(dirname "${LOG_FILE}")"
    
    # Create status files directory
    mkdir -p "${PROJECT_DIR}/monitoring/status"
    
    log "Monitoring initialized. Log file: ${LOG_FILE}"
}

# Check HTTP endpoint health
check_http_health() {
    local url="$1"
    local timeout="${2:-10}"
    
    if curl -f -s --max-time "${timeout}" "${url}" >/dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# Check Docker container health
check_container_health() {
    local container_name="$1"
    
    if docker ps --filter "name=${container_name}" --filter "status=running" --format "{{.Names}}" | grep -q "^${container_name}$"; then
        # Check if container has health check
        local health_status
        health_status=$(docker inspect --format='{{.State.Health.Status}}' "${container_name}" 2>/dev/null || echo "no-healthcheck")
        
        if [[ "${health_status}" == "healthy" ]] || [[ "${health_status}" == "no-healthcheck" ]]; then
            return 0
        else
            return 1
        fi
    else
        return 1
    fi
}

# Check database connectivity
check_database_health() {
    local env="$1"
    local port="$2"
    
    local container_name
    if [[ "${env}" == "development" ]]; then
        container_name="guardianes-mysql"
    else
        container_name="guardianes-mysql-${env}"
    fi
    
    if check_container_health "${container_name}"; then
        # Test actual database connectivity
        if docker exec "${container_name}" mysqladmin ping -h localhost >/dev/null 2>&1; then
            return 0
        else
            return 1
        fi
    else
        return 1
    fi
}

# Check Redis connectivity
check_redis_health() {
    local env="$1"
    
    local container_name
    if [[ "${env}" == "development" ]]; then
        container_name="guardianes-redis"
    else
        container_name="guardianes-redis-${env}"
    fi
    
    if check_container_health "${container_name}"; then
        # Test Redis ping
        if docker exec "${container_name}" redis-cli ping >/dev/null 2>&1; then
            return 0
        else
            return 1
        fi
    else
        return 1
    fi
}

# Check RabbitMQ health
check_rabbitmq_health() {
    local env="$1"
    
    local container_name
    if [[ "${env}" == "development" ]]; then
        container_name="guardianes-rabbitmq"
    else
        container_name="guardianes-rabbitmq-${env}"
    fi
    
    if check_container_health "${container_name}"; then
        # Test RabbitMQ status
        if docker exec "${container_name}" rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
            return 0
        else
            return 1
        fi
    else
        return 1
    fi
}

# Check disk space
check_disk_space() {
    local threshold=85  # Alert if disk usage > 85%
    
    local disk_usage
    disk_usage=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
    
    if [[ "${disk_usage}" -gt "${threshold}" ]]; then
        warning "Disk usage is ${disk_usage}% (threshold: ${threshold}%)"
        return 1
    else
        return 0
    fi
}

# Check memory usage
check_memory_usage() {
    local threshold=90  # Alert if memory usage > 90%
    
    local memory_usage
    memory_usage=$(free | awk 'NR==2{printf "%.0f", $3*100/$2}')
    
    if [[ "${memory_usage}" -gt "${threshold}" ]]; then
        warning "Memory usage is ${memory_usage}% (threshold: ${threshold}%)"
        return 1
    else
        return 0
    fi
}

# Check SSL certificate expiration
check_ssl_expiration() {
    local domain="$1"
    local warning_days=30  # Warn if certificate expires within 30 days
    
    if command -v openssl >/dev/null 2>&1; then
        local cert_path="/etc/letsencrypt/live/${domain}/fullchain.pem"
        
        if [[ -f "${cert_path}" ]]; then
            local exp_date
            exp_date=$(openssl x509 -in "${cert_path}" -noout -enddate | cut -d= -f2)
            
            local exp_timestamp
            exp_timestamp=$(date -d "${exp_date}" +%s)
            
            local current_timestamp
            current_timestamp=$(date +%s)
            
            local days_until_expiration
            days_until_expiration=$(( (exp_timestamp - current_timestamp) / 86400 ))
            
            if [[ "${days_until_expiration}" -lt "${warning_days}" ]]; then
                warning "SSL certificate for ${domain} expires in ${days_until_expiration} days"
                return 1
            else
                return 0
            fi
        else
            warning "SSL certificate not found for ${domain}"
            return 1
        fi
    else
        return 0  # Skip if openssl not available
    fi
}

# Comprehensive environment health check
check_environment_health() {
    local env="$1"
    local port="$2"
    local domain="$3"
    
    local checks_passed=0
    local total_checks=6
    
    log "Checking health of ${env} environment..."
    
    # 1. HTTP Health Check
    if check_http_health "http://localhost:${port}/actuator/health"; then
        success "${env}: Backend HTTP health check passed"
        ((checks_passed++))
    else
        error "${env}: Backend HTTP health check failed"
    fi
    
    # 2. Database Health Check
    if check_database_health "${env}" "${port}"; then
        success "${env}: Database health check passed"
        ((checks_passed++))
    else
        error "${env}: Database health check failed"
    fi
    
    # 3. Redis Health Check
    if check_redis_health "${env}"; then
        success "${env}: Redis health check passed"
        ((checks_passed++))
    else
        error "${env}: Redis health check failed"
    fi
    
    # 4. RabbitMQ Health Check
    if check_rabbitmq_health "${env}"; then
        success "${env}: RabbitMQ health check passed"
        ((checks_passed++))
    else
        error "${env}: RabbitMQ health check failed"
    fi
    
    # 5. SSL Certificate Check (production only)
    if [[ "${env}" == "production" ]]; then
        if check_ssl_expiration "${domain}"; then
            success "${env}: SSL certificate check passed"
            ((checks_passed++))
        else
            error "${env}: SSL certificate check failed"
        fi
    else
        success "${env}: SSL certificate check skipped (not production)"
        ((checks_passed++))
    fi
    
    # 6. External Domain Check (if domain provided)
    if [[ -n "${domain}" ]] && [[ "${domain}" != "localhost" ]]; then
        local protocol="http"
        if [[ "${env}" == "production" ]]; then
            protocol="https"
        fi
        
        if check_http_health "${protocol}://${domain}/actuator/health"; then
            success "${env}: External domain health check passed"
            ((checks_passed++))
        else
            error "${env}: External domain health check failed"
        fi
    else
        success "${env}: External domain check skipped"
        ((checks_passed++))
    fi
    
    # Calculate health percentage
    local health_percentage
    health_percentage=$(( checks_passed * 100 / total_checks ))
    
    log "${env}: Health check completed - ${checks_passed}/${total_checks} checks passed (${health_percentage}%)"
    
    # Save status
    echo "${health_percentage}" > "${PROJECT_DIR}/monitoring/status/${env}.status"
    
    return $((total_checks - checks_passed))
}

# Check system resources
check_system_health() {
    log "Checking system health..."
    
    local checks_passed=0
    local total_checks=2
    
    # Disk space check
    if check_disk_space; then
        success "System: Disk space check passed"
        ((checks_passed++))
    else
        error "System: Disk space check failed"
    fi
    
    # Memory usage check
    if check_memory_usage; then
        success "System: Memory usage check passed"
        ((checks_passed++))
    else
        error "System: Memory usage check failed"
    fi
    
    log "System health: ${checks_passed}/${total_checks} checks passed"
    
    return $((total_checks - checks_passed))
}

# Send alert notification
send_alert() {
    local message="$1"
    local severity="${2:-warning}"
    
    # Log the alert
    if [[ "${severity}" == "critical" ]]; then
        error "ALERT: ${message}"
    else
        warning "ALERT: ${message}"
    fi
    
    # Here you could add integrations with:
    # - Email notifications
    # - Slack/Discord webhooks
    # - SMS alerts
    # - PagerDuty
    # - etc.
    
    # Example webhook (uncomment and configure):
    # curl -X POST -H 'Content-type: application/json' \
    #   --data "{\"text\":\"${message}\"}" \
    #   "${SLACK_WEBHOOK_URL}" || true
}

# Run single health check cycle
run_health_check_cycle() {
    log "Starting health check cycle"
    
    local total_failures=0
    
    # Check each environment
    for env_config in "${ENVIRONMENTS[@]}"; do
        IFS=':' read -r env port domain <<< "${env_config}"
        
        if ! check_environment_health "${env}" "${port}" "${domain}"; then
            ((total_failures++))
        fi
        
        echo  # Add spacing between environments
    done
    
    # Check system health
    if ! check_system_health; then
        ((total_failures++))
    fi
    
    # Generate summary
    if [[ "${total_failures}" -eq 0 ]]; then
        success "All health checks passed"
    else
        warning "${total_failures} health check(s) failed"
        
        # Send alert if threshold reached
        if [[ "${total_failures}" -ge "${ALERT_THRESHOLD}" ]]; then
            send_alert "Multiple health checks failing (${total_failures} failures)" "critical"
        fi
    fi
    
    log "Health check cycle completed"
}

# Continuous monitoring mode
run_continuous_monitoring() {
    log "Starting continuous monitoring (interval: ${CHECK_INTERVAL}s)"
    
    while true; do
        run_health_check_cycle
        echo
        log "Next check in ${CHECK_INTERVAL} seconds..."
        sleep "${CHECK_INTERVAL}"
    done
}

# Generate health report
generate_health_report() {
    log "Generating health report..."
    
    local report_file="${PROJECT_DIR}/monitoring/health_report_$(date +%Y%m%d_%H%M%S).html"
    
    cat > "${report_file}" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Guardianes de Gaia - Health Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #2c3e50; color: white; padding: 20px; border-radius: 5px; }
        .environment { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .healthy { background: #d4edda; border-color: #c3e6cb; }
        .warning { background: #fff3cd; border-color: #ffeaa7; }
        .critical { background: #f8d7da; border-color: #f5c6cb; }
        .status { font-weight: bold; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Guardianes de Gaia - Health Report</h1>
        <p>Generated: $(date)</p>
    </div>
EOF
    
    # Add environment status
    for env_config in "${ENVIRONMENTS[@]}"; do
        IFS=':' read -r env port domain <<< "${env_config}"
        
        local status_file="${PROJECT_DIR}/monitoring/status/${env}.status"
        local health_percentage=0
        
        if [[ -f "${status_file}" ]]; then
            health_percentage=$(cat "${status_file}")
        fi
        
        local status_class="critical"
        if [[ "${health_percentage}" -ge 80 ]]; then
            status_class="healthy"
        elif [[ "${health_percentage}" -ge 60 ]]; then
            status_class="warning"
        fi
        
        cat >> "${report_file}" << EOF
    <div class="environment ${status_class}">
        <h2>${env^} Environment</h2>
        <p class="status">Health: ${health_percentage}%</p>
        <p>Port: ${port}</p>
        <p>Domain: ${domain}</p>
    </div>
EOF
    done
    
    echo "    </body></html>" >> "${report_file}"
    
    success "Health report generated: ${report_file}"
}

# Show usage
show_usage() {
    echo "Guardianes de Gaia - Health Monitoring Script"
    echo
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo
    echo "Commands:"
    echo "  check         Run single health check cycle"
    echo "  monitor       Start continuous monitoring"
    echo "  report        Generate health report"
    echo "  status        Show current status"
    echo
    echo "Options:"
    echo "  --interval N  Set check interval in seconds (default: 60)"
    echo "  --threshold N Set alert threshold (default: 3)"
    echo
    echo "Examples:"
    echo "  $0 check                    # Run single health check"
    echo "  $0 monitor --interval 30    # Continuous monitoring every 30s"
    echo "  $0 report                   # Generate HTML health report"
}

# Show current status
show_status() {
    log "Current system status:"
    echo
    
    for env_config in "${ENVIRONMENTS[@]}"; do
        IFS=':' read -r env port domain <<< "${env_config}"
        
        local status_file="${PROJECT_DIR}/monitoring/status/${env}.status"
        local health_percentage="Unknown"
        
        if [[ -f "${status_file}" ]]; then
            health_percentage=$(cat "${status_file}")
        fi
        
        echo "${env^}: ${health_percentage}%"
    done
}

# Main function
main() {
    local command="${1:-check}"
    shift || true
    
    # Parse options
    while [[ $# -gt 0 ]]; do
        case $1 in
            --interval)
                CHECK_INTERVAL="$2"
                shift 2
                ;;
            --threshold)
                ALERT_THRESHOLD="$2"
                shift 2
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # Initialize monitoring
    init_monitoring
    
    # Execute command
    case "${command}" in
        check)
            run_health_check_cycle
            ;;
        monitor)
            run_continuous_monitoring
            ;;
        report)
            generate_health_report
            ;;
        status)
            show_status
            ;;
        *)
            error "Unknown command: ${command}"
            show_usage
            exit 1
            ;;
    esac
}

# Script execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi