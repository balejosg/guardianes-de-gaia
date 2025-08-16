#!/bin/bash
# Guardianes de Gaia - Multi-Environment Deployment Script
# Manages deployment across development, staging, and production environments

# Check if we're running in bash
if [ -z "${BASH_VERSION:-}" ]; then
    echo "This script requires bash. Please run with: bash $0 $*"
    exit 1
fi

set -euo pipefail

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
DEPLOY_DIR="${PROJECT_DIR}/deploy"

# Configuration
ENVIRONMENTS=("development" "staging" "production")
DEFAULT_ENVIRONMENT="development"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Logging functions
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

info() {
    echo -e "${CYAN}[INFO]${NC} $1"
}

# Show usage information
show_usage() {
    echo "Guardianes de Gaia - Multi-Environment Deployment Script"
    echo
    echo "Usage: $0 [COMMAND] [ENVIRONMENT] [OPTIONS]"
    echo
    echo "Commands:"
    echo "  deploy        Deploy specific environment"
    echo "  start         Start environment services"
    echo "  stop          Stop environment services"
    echo "  restart       Restart environment services"
    echo "  status        Show environment status"
    echo "  logs          Show environment logs"
    echo "  backup        Backup environment data"
    echo "  update        Update environment (rebuild and redeploy)"
    echo "  health        Check environment health"
    echo "  cleanup       Clean unused Docker resources"
    echo "  init          Initialize environment (first-time setup)"
    echo
    echo "Environments:"
    echo "  development   Development environment (port 8080)"
    echo "  staging       Staging environment (port 8081)"
    echo "  production    Production environment (port 8082)"
    echo "  all           All environments"
    echo
    echo "Options:"
    echo "  --build       Force rebuild of images"
    echo "  --no-cache    Build without cache"
    echo "  --pull        Pull latest base images"
    echo "  --force       Force action (use with caution)"
    echo "  --verbose     Verbose output"
    echo "  --dry-run     Show what would be done without executing"
    echo
    echo "Examples:"
    echo "  $0 deploy development          # Deploy development environment"
    echo "  $0 start staging --verbose     # Start staging with verbose output"
    echo "  $0 update production --build   # Update production with rebuild"
    echo "  $0 status all                  # Show status of all environments"
    echo "  $0 init development            # Initialize development environment"
}

# Parse command line arguments
parse_arguments() {
    COMMAND=""
    ENVIRONMENT=""
    BUILD_FLAG=""
    NO_CACHE_FLAG=""
    PULL_FLAG=""
    FORCE_FLAG=""
    VERBOSE_FLAG=""
    DRY_RUN_FLAG=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            deploy|start|stop|restart|status|logs|backup|update|health|cleanup|init)
                COMMAND="$1"
                shift
                ;;
            development|staging|production|all)
                ENVIRONMENT="$1"
                shift
                ;;
            --build)
                BUILD_FLAG="--build"
                shift
                ;;
            --no-cache)
                NO_CACHE_FLAG="--no-cache"
                shift
                ;;
            --pull)
                PULL_FLAG="--pull"
                shift
                ;;
            --force)
                FORCE_FLAG="--force"
                shift
                ;;
            --verbose)
                VERBOSE_FLAG="--verbose"
                shift
                ;;
            --dry-run)
                DRY_RUN_FLAG="--dry-run"
                shift
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
    
    # Set default environment if not specified
    if [[ -z "${ENVIRONMENT}" && "${COMMAND}" != "cleanup" ]]; then
        ENVIRONMENT="${DEFAULT_ENVIRONMENT}"
    fi
    
    # Validate command
    if [[ -z "${COMMAND}" ]]; then
        error "Command is required"
        show_usage
        exit 1
    fi
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker >/dev/null 2>&1; then
        error "Docker is not installed"
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose >/dev/null 2>&1; then
        error "Docker Compose is not installed"
        exit 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info >/dev/null 2>&1; then
        error "Docker daemon is not running"
        exit 1
    fi
    
    success "Prerequisites check passed"
}

# Setup environment directories
setup_directories() {
    local env="$1"
    
    log "Setting up directories for ${env} environment..."
    
    local data_dir="${HOME}/guardianes-data/${env}"
    local logs_dir="${PROJECT_DIR}/logs/${env}"
    local backup_dir="${PROJECT_DIR}/backup/${env}"
    
    # Create data directories
    mkdir -p "${data_dir}"/{mysql,redis,rabbitmq,prometheus,grafana}
    
    # Create logs directories
    mkdir -p "${logs_dir}"/{mysql,redis,rabbitmq,grafana,nginx}
    
    # Create backup directories
    mkdir -p "${backup_dir}"/{mysql,redis,rabbitmq}
    
    # Set proper permissions
    chmod -R 755 "${data_dir}"
    chmod -R 755 "${logs_dir}"
    chmod -R 755 "${backup_dir}"
    
    success "Directories setup completed for ${env}"
}

# Get Docker Compose file for environment
get_compose_file() {
    local env="$1"
    
    case "${env}" in
        development)
            echo "${PROJECT_DIR}/docker-compose.yml"
            ;;
        staging)
            echo "-f ${PROJECT_DIR}/docker-compose.yml -f ${DEPLOY_DIR}/docker-compose.staging.yml"
            ;;
        production)
            echo "-f ${PROJECT_DIR}/docker-compose.yml -f ${DEPLOY_DIR}/docker-compose.production.yml"
            ;;
        *)
            error "Unknown environment: ${env}"
            exit 1
            ;;
    esac
}

# Execute Docker Compose command
execute_compose() {
    local env="$1"
    local cmd="$2"
    shift 2
    
    local compose_files
    compose_files="$(get_compose_file "${env}")"
    
    # For development, check single file; for staging/production, validate all files
    if [[ "${env}" == "development" ]]; then
        if [[ ! -f "${compose_files}" ]]; then
            error "Docker Compose file not found: ${compose_files}"
            exit 1
        fi
        local compose_cmd="docker-compose -f ${compose_files}"
    else
        # For staging/production, we have multiple files
        local compose_cmd="docker-compose ${compose_files}"
        # Validate that base compose file exists
        if [[ ! -f "${PROJECT_DIR}/docker-compose.yml" ]]; then
            error "Base Docker Compose file not found: ${PROJECT_DIR}/docker-compose.yml"
            exit 1
        fi
    fi
    
    log "Executing: ${compose_cmd} ${cmd} $*"
    
    if [[ -n "${DRY_RUN_FLAG}" ]]; then
        info "DRY RUN: Would execute ${compose_cmd} ${cmd} $*"
        return 0
    fi
    
    cd "${PROJECT_DIR}"
    eval "${compose_cmd} ${cmd} $*"
}

# Deploy environment
deploy_environment() {
    local env="$1"
    
    log "Deploying ${env} environment..."
    
    # Setup directories
    setup_directories "${env}"
    
    # Build and start services
    local build_args=""
    if [[ -n "${BUILD_FLAG}" ]]; then
        build_args="${BUILD_FLAG}"
    fi
    if [[ -n "${NO_CACHE_FLAG}" ]]; then
        build_args="${build_args} ${NO_CACHE_FLAG}"
    fi
    if [[ -n "${PULL_FLAG}" ]]; then
        build_args="${build_args} ${PULL_FLAG}"
    fi
    
    execute_compose "${env}" "up -d" ${build_args}
    
    # Wait for services to be healthy
    wait_for_health "${env}"
    
    success "${env} environment deployed successfully"
}

# Start environment
start_environment() {
    local env="$1"
    
    log "Starting ${env} environment..."
    execute_compose "${env}" "start"
    success "${env} environment started"
}

# Stop environment
stop_environment() {
    local env="$1"
    
    log "Stopping ${env} environment..."
    execute_compose "${env}" "stop"
    success "${env} environment stopped"
}

# Restart environment
restart_environment() {
    local env="$1"
    
    log "Restarting ${env} environment..."
    execute_compose "${env}" "restart"
    success "${env} environment restarted"
}

# Show environment status
show_status() {
    local env="$1"
    
    log "Status for ${env} environment:"
    execute_compose "${env}" "ps"
}

# Show environment logs
show_logs() {
    local env="$1"
    
    log "Logs for ${env} environment:"
    execute_compose "${env}" "logs -f --tail=100"
}

# Backup environment
backup_environment() {
    local env="$1"
    
    log "Backing up ${env} environment..."
    
    local backup_dir="${PROJECT_DIR}/backup/${env}/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "${backup_dir}"
    
    # Backup MySQL
    if docker ps --format "table {{.Names}}" | grep -q "mysql-${env}\|guardianes-mysql"; then
        log "Backing up MySQL database..."
        local container_name
        if [[ "${env}" == "development" ]]; then
            container_name="guardianes-mysql"
        else
            container_name="guardianes-mysql-${env}"
        fi
        
        docker exec "${container_name}" mysqldump -u root -p"${DB_ROOT_PASSWORD:-rootsecret}" --all-databases > "${backup_dir}/mysql_backup.sql"
        success "MySQL backup completed"
    fi
    
    # Backup Redis
    if docker ps --format "table {{.Names}}" | grep -q "redis-${env}\|guardianes-redis"; then
        log "Backing up Redis data..."
        local container_name
        if [[ "${env}" == "development" ]]; then
            container_name="guardianes-redis"
        else
            container_name="guardianes-redis-${env}"
        fi
        
        docker exec "${container_name}" redis-cli --rdb "${backup_dir}/redis_backup.rdb" || true
        success "Redis backup completed"
    fi
    
    success "Backup completed: ${backup_dir}"
}

# Update environment
update_environment() {
    local env="$1"
    
    log "Updating ${env} environment..."
    
    # Pull latest images if requested
    if [[ -n "${PULL_FLAG}" ]]; then
        execute_compose "${env}" "pull"
    fi
    
    # Rebuild and redeploy
    execute_compose "${env}" "up -d --build"
    
    success "${env} environment updated"
}

# Check environment health
check_health() {
    local env="$1"
    
    log "Checking health of ${env} environment..."
    
    local health_url
    case "${env}" in
        development) health_url="http://dev-guardianes.duckdns.org/actuator/health" ;;
        staging) health_url="http://stg-guardianes.duckdns.org/actuator/health" ;;
        production) health_url="http://guardianes.duckdns.org/actuator/health" ;;
    esac
    
    # Check backend health via nginx proxy
    log "Testing health endpoint: ${health_url}"
    if curl -f "${health_url}" >/dev/null 2>&1; then
        success "Backend service is healthy"
    else
        warning "Backend service health check failed (may require authentication)"
        # Try with basic auth if available
        if curl -f -u admin:admin "${health_url}" >/dev/null 2>&1; then
            success "Backend service is healthy (with authentication)"
        else
            error "Backend service is not healthy"
        fi
    fi
    
    # Check container health
    execute_compose "${env}" "ps"
}

# Wait for services to be healthy
wait_for_health() {
    local env="$1"
    local max_attempts=30
    local attempt=1
    
    log "Waiting for ${env} services to be healthy..."
    
    local health_url
    case "${env}" in
        development) health_url="http://dev-guardianes.duckdns.org/actuator/health" ;;
        staging) health_url="http://stg-guardianes.duckdns.org/actuator/health" ;;
        production) health_url="http://guardianes.duckdns.org/actuator/health" ;;
    esac
    
    while [[ ${attempt} -le ${max_attempts} ]]; do
        # Check health endpoint via nginx proxy
        if curl -f "${health_url}" >/dev/null 2>&1; then
            success "Services are healthy after ${attempt} attempts"
            return 0
        fi
        
        # Try with authentication if direct access fails
        if curl -f -u admin:admin "${health_url}" >/dev/null 2>&1; then
            success "Services are healthy (with authentication) after ${attempt} attempts"
            return 0
        fi
        
        log "Attempt ${attempt}/${max_attempts}: Services not ready yet, waiting 10 seconds..."
        sleep 10
        ((attempt++))
    done
    
    warning "Services may not be fully healthy after ${max_attempts} attempts"
    warning "Try checking manually: ${health_url}"
}

# Initialize environment (first-time setup)
init_environment() {
    local env="$1"
    
    log "Initializing ${env} environment..."
    
    # Check if environment is already running
    if docker ps --format "table {{.Names}}" | grep -q "${env}"; then
        if [[ -z "${FORCE_FLAG}" ]]; then
            warning "${env} environment appears to be running. Use --force to reinitialize."
            exit 1
        fi
        
        log "Force flag detected. Stopping existing environment..."
        stop_environment "${env}"
    fi
    
    # Setup directories
    setup_directories "${env}"
    
    # Load environment-specific configuration
    load_environment_config "${env}"
    
    # Deploy environment
    deploy_environment "${env}"
    
    success "${env} environment initialized successfully"
}

# Load environment-specific configuration
load_environment_config() {
    local env="$1"
    
    local env_file="${DEPLOY_DIR}/.env.${env}"
    
    if [[ -f "${env_file}" ]]; then
        log "Loading environment configuration from ${env_file}"
        # shellcheck source=/dev/null
        source "${env_file}"
    else
        warning "Environment configuration file not found: ${env_file}"
    fi
}

# Cleanup unused Docker resources
cleanup_docker() {
    log "Cleaning up unused Docker resources..."
    
    if [[ -n "${DRY_RUN_FLAG}" ]]; then
        info "DRY RUN: Would clean up Docker resources"
        return 0
    fi
    
    docker system prune -f
    docker volume prune -f
    
    success "Docker cleanup completed"
}

# Execute command for single environment
execute_for_environment() {
    local cmd="$1"
    local env="$2"
    
    case "${cmd}" in
        deploy)
            deploy_environment "${env}"
            ;;
        start)
            start_environment "${env}"
            ;;
        stop)
            stop_environment "${env}"
            ;;
        restart)
            restart_environment "${env}"
            ;;
        status)
            show_status "${env}"
            ;;
        logs)
            show_logs "${env}"
            ;;
        backup)
            backup_environment "${env}"
            ;;
        update)
            update_environment "${env}"
            ;;
        health)
            check_health "${env}"
            ;;
        init)
            init_environment "${env}"
            ;;
        *)
            error "Unknown command: ${cmd}"
            exit 1
            ;;
    esac
}

# Execute command for all environments
execute_for_all() {
    local cmd="$1"
    
    for env in "${ENVIRONMENTS[@]}"; do
        log "Processing ${env} environment..."
        execute_for_environment "${cmd}" "${env}"
        echo
    done
}

# Main execution function
main() {
    log "Guardianes de Gaia - Multi-Environment Deployment Script"
    
    # Parse arguments
    parse_arguments "$@"
    
    # Check prerequisites
    check_prerequisites
    
    # Handle cleanup command (no environment needed)
    if [[ "${COMMAND}" == "cleanup" ]]; then
        cleanup_docker
        exit 0
    fi
    
    # Execute command
    if [[ "${ENVIRONMENT}" == "all" ]]; then
        execute_for_all "${COMMAND}"
    else
        execute_for_environment "${COMMAND}" "${ENVIRONMENT}"
    fi
    
    success "Operation completed successfully"
}

# Script execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi