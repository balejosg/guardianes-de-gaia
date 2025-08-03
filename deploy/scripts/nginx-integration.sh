#!/bin/bash
# Guardianes de Gaia - Nginx Integration Script
# Integrates Guardianes configuration with existing nginx setup

# Check if we're running in bash
if [ -z "${BASH_VERSION:-}" ]; then
    echo "This script requires bash. Please run with: bash $0 $*"
    exit 1
fi

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# Logging functions
log() {
    echo -e "${BLUE}[NGINX-INTEGRATION]${NC} $1"
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

info() {
    echo -e "${CYAN}[INFO]${NC} $1"
}

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"

show_usage() {
    echo "Nginx Integration Options for Guardianes de Gaia"
    echo
    echo "Usage: $0 [OPTION]"
    echo
    echo "Options:"
    echo "  analyze     Analyze current nginx configuration"
    echo "  additional  Add Guardianes as additional site (recommended)"
    echo "  integrate   Integrate into existing configuration"
    echo "  separate    Use separate ports for Guardianes"
    echo "  restore     Restore from backup"
    echo
    echo "Examples:"
    echo "  $0 analyze     # Analyze current setup"
    echo "  $0 additional  # Add as additional site"
    echo "  $0 separate    # Use separate ports"
}

# Analyze current nginx configuration
analyze_current_config() {
    log "Analyzing current nginx configuration..."
    
    echo -e "\n${CYAN}=== Current Configuration Analysis ===${NC}"
    
    # Check if nginx is running
    if systemctl is-active --quiet nginx; then
        success "Nginx is currently running"
    else
        warning "Nginx is not running"
    fi
    
    # Check main configuration
    echo -e "\n${BLUE}Main Configuration:${NC}"
    if [[ -f /etc/nginx/nginx.conf ]]; then
        success "Main nginx.conf exists"
        echo "Include paths:"
        grep -E "include.*sites-" /etc/nginx/nginx.conf || echo "  No sites-* includes found"
    else
        error "Main nginx.conf not found"
    fi
    
    # Check sites
    echo -e "\n${BLUE}Sites Configuration:${NC}"
    if [[ -d /etc/nginx/sites-available ]]; then
        local available_sites
        available_sites=$(ls -1 /etc/nginx/sites-available/ 2>/dev/null | wc -l)
        echo "Available sites: ${available_sites}"
        ls /etc/nginx/sites-available/ 2>/dev/null | head -5
    fi
    
    if [[ -d /etc/nginx/sites-enabled ]]; then
        local enabled_sites
        enabled_sites=$(ls -1 /etc/nginx/sites-enabled/ 2>/dev/null | wc -l)
        echo "Enabled sites: ${enabled_sites}"
        ls -la /etc/nginx/sites-enabled/ 2>/dev/null
    fi
    
    # Check for existing domains/server blocks
    echo -e "\n${BLUE}Existing Domains:${NC}"
    if command -v nginx >/dev/null && nginx -T >/dev/null 2>&1; then
        nginx -T 2>/dev/null | grep -E "server_name" | head -10 || echo "No server_name directives found"
    else
        warning "Cannot parse nginx configuration (syntax errors?)"
    fi
    
    # Check for port conflicts
    echo -e "\n${BLUE}Port Usage:${NC}"
    nginx -T 2>/dev/null | grep -E "listen.*80|listen.*443" | sort | uniq || echo "No port listeners found"
    
    # Check SSL configuration
    echo -e "\n${BLUE}SSL Configuration:${NC}"
    if [[ -d /etc/letsencrypt/live ]]; then
        echo "Let's Encrypt certificates:"
        ls /etc/letsencrypt/live/ 2>/dev/null || echo "  No certificates found"
    else
        echo "No Let's Encrypt directory found"
    fi
    
    echo -e "\n${GREEN}Analysis complete.${NC}"
    echo -e "\n${YELLOW}Recommendations:${NC}"
    
    # Provide recommendations based on analysis
    if [[ -f /etc/nginx/sites-available/default ]]; then
        echo "• You have a default site - consider using 'additional' option"
    fi
    
    if nginx -T 2>/dev/null | grep -q "listen.*80"; then
        echo "• Port 80 is in use - Guardianes will integrate with existing HTTP setup"
    fi
    
    if nginx -T 2>/dev/null | grep -q "listen.*443"; then
        echo "• Port 443 is in use - Guardianes SSL will integrate with existing HTTPS setup"
    fi
    
    echo "• Recommended approach: 'additional' (safest, preserves existing setup)"
}

# Add Guardianes as additional site
add_as_additional_site() {
    log "Adding Guardianes as additional site..."
    
    # Backup current config using safe backup script
    log "Creating backup of current nginx configuration..."
    local backup_dir
    backup_dir=$(bash "${SCRIPT_DIR}/backup-nginx.sh")
    success "Current configuration backed up to ${backup_dir}"
    
    # Copy OMV-compatible Guardianes configuration
    if [[ -f "${PROJECT_DIR}/deploy/nginx/guardianes-omv.conf" ]]; then
        sudo cp "${PROJECT_DIR}/deploy/nginx/guardianes-omv.conf" /etc/nginx/sites-available/guardianes.conf
        success "OMV-compatible Guardianes configuration copied to sites-available"
    else
        # Fallback to original configuration
        sudo cp "${PROJECT_DIR}/deploy/nginx/guardianes.conf" /etc/nginx/sites-available/
        success "Guardianes configuration copied to sites-available"
    fi
    
    # Check for conflicts
    if [[ -f /etc/nginx/sites-available/guardianes.conf ]] && [[ -f /etc/nginx/sites-enabled/guardianes.conf ]]; then
        warning "Guardianes configuration already exists and is enabled"
        return 0
    fi
    
    # Enable the site
    sudo ln -sf /etc/nginx/sites-available/guardianes.conf /etc/nginx/sites-enabled/
    success "Guardianes site enabled"
    
    # Test configuration
    if sudo nginx -t; then
        success "Nginx configuration test passed"
        
        # Reload nginx
        sudo systemctl reload nginx
        success "Nginx reloaded successfully"
        
        echo -e "\n${GREEN}✅ Guardianes added as additional site successfully!${NC}"
        echo -e "\n${CYAN}Your domains:${NC}"
        echo "• dev-guardianes.duckdns.org → Development environment"
        echo "• stg-guardianes.duckdns.org → Staging environment"
        echo "• guardianes.duckdns.org → Production environment"
        echo -e "\n${YELLOW}Your existing sites remain unchanged.${NC}"
        
    else
        error "Nginx configuration test failed"
        echo "Rolling back changes..."
        sudo rm -f /etc/nginx/sites-enabled/guardianes.conf
        sudo rm -f /etc/nginx/sites-available/guardianes.conf
        return 1
    fi
}

# Integrate into existing configuration
integrate_into_existing() {
    log "Integrating Guardianes into existing configuration..."
    
    warning "This option modifies your existing nginx configuration."
    echo "This will:"
    echo "• Add Guardianes server blocks to your existing configuration"
    echo "• Preserve your existing sites"
    echo "• May require manual adjustment of conflicts"
    echo
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        log "Integration cancelled"
        return 0
    fi
    
    # Create backup using safe backup script
    log "Creating backup of current nginx configuration..."
    local backup_dir
    backup_dir=$(bash "${SCRIPT_DIR}/backup-nginx.sh")
    success "Configuration backed up to ${backup_dir}"
    
    # Extract server blocks from Guardianes config
    log "Extracting Guardianes server blocks..."
    
    # Create a modified version that integrates with existing setup
    local temp_config="/tmp/guardianes-integrated.conf"
    
    cat > "${temp_config}" << 'EOF'
# Guardianes de Gaia server blocks - integrated with existing nginx
# Added to existing configuration

# Rate limiting zones for Guardianes
limit_req_zone $binary_remote_addr zone=guardianes_api:10m rate=10r/s;
limit_req_zone $binary_remote_addr zone=guardianes_auth:10m rate=5r/s;

EOF
    
    # Extract server blocks from our config (skip the global settings)
    sed -n '/^server {/,/^}/p' "${PROJECT_DIR}/deploy/nginx/guardianes.conf" >> "${temp_config}"
    
    # Check if we can append to an existing file or need to create a new one
    local target_file="/etc/nginx/sites-available/guardianes-integrated"
    sudo cp "${temp_config}" "${target_file}"
    sudo ln -sf "${target_file}" /etc/nginx/sites-enabled/
    
    success "Guardianes server blocks integrated"
    
    # Test configuration
    if sudo nginx -t; then
        success "Nginx configuration test passed"
        sudo systemctl reload nginx
        success "Nginx reloaded successfully"
        
        echo -e "\n${GREEN}✅ Guardianes integrated into existing configuration!${NC}"
    else
        error "Nginx configuration test failed"
        echo "Rolling back changes..."
        sudo rm -f /etc/nginx/sites-enabled/guardianes-integrated
        sudo rm -f "${target_file}"
        return 1
    fi
    
    rm -f "${temp_config}"
}

# Use separate ports for Guardianes
setup_separate_ports() {
    log "Setting up Guardianes on separate ports..."
    
    info "This approach runs Guardianes nginx on separate ports:"
    echo "• Guardianes nginx: ports 8080, 8081, 8082"
    echo "• Your existing nginx: keeps current ports (80, 443)"
    echo "• External access via reverse proxy or port forwarding"
    
    # Create separate nginx configuration for Guardianes
    local guardianes_nginx_dir="/opt/guardianes-nginx"
    sudo mkdir -p "${guardianes_nginx_dir}"/{conf.d,ssl,logs}
    
    # Create Guardianes-specific nginx.conf
    cat > "/tmp/guardianes-nginx.conf" << EOF
user www-data;
worker_processes auto;
pid ${guardianes_nginx_dir}/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;
    
    access_log ${guardianes_nginx_dir}/logs/access.log;
    error_log ${guardianes_nginx_dir}/logs/error.log;
    
    # Development Environment
    server {
        listen 8080;
        server_name dev-guardianes.duckdns.org localhost;
        
        location / {
            proxy_pass http://127.0.0.1:8080;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
        }
    }
    
    # Staging Environment
    server {
        listen 8081;
        server_name stg-guardianes.duckdns.org;
        
        location / {
            proxy_pass http://127.0.0.1:8081;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
        }
    }
    
    # Production Environment
    server {
        listen 8082 ssl;
        server_name guardianes.duckdns.org;
        
        ssl_certificate /etc/letsencrypt/live/guardianes.duckdns.org/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/guardianes.duckdns.org/privkey.pem;
        
        location / {
            proxy_pass http://127.0.0.1:8082;
            proxy_set_header Host \$host;
            proxy_set_header X-Real-IP \$remote_addr;
        }
    }
}
EOF
    
    sudo mv "/tmp/guardianes-nginx.conf" "${guardianes_nginx_dir}/nginx.conf"
    
    # Create systemd service for Guardianes nginx
    sudo tee /etc/systemd/system/guardianes-nginx.service << EOF
[Unit]
Description=Guardianes de Gaia Nginx
After=network.target

[Service]
Type=forking
PIDFile=${guardianes_nginx_dir}/nginx.pid
ExecStartPre=/usr/sbin/nginx -t -c ${guardianes_nginx_dir}/nginx.conf
ExecStart=/usr/sbin/nginx -c ${guardianes_nginx_dir}/nginx.conf
ExecReload=/bin/kill -s HUP \$MAINPID
KillMode=mixed

[Install]
WantedBy=multi-user.target
EOF
    
    sudo systemctl daemon-reload
    sudo systemctl enable guardianes-nginx
    
    success "Guardianes nginx service created"
    echo -e "\n${CYAN}To start Guardianes nginx:${NC}"
    echo "sudo systemctl start guardianes-nginx"
    echo -e "\n${CYAN}Access URLs:${NC}"
    echo "• Development: http://your-server:8080"
    echo "• Staging: http://your-server:8081"
    echo "• Production: https://your-server:8082"
    echo -e "\n${YELLOW}Note: You'll need to configure port forwarding in your router${NC}"
    echo "for external access or use a reverse proxy from your main nginx."
}

# Restore from backup
restore_from_backup() {
    log "Available nginx backups:"
    
    local backups
    backups=$(find /etc/nginx -maxdepth 1 -type d -name "backup-*" 2>/dev/null | sort -r)
    
    if [[ -z "${backups}" ]]; then
        warning "No automatic backups found in /etc/nginx/"
        echo "Manual backups might be in ~/nginx-backup-* directories"
        return 1
    fi
    
    echo "Available backups:"
    local i=1
    while IFS= read -r backup; do
        echo "${i}. ${backup}"
        ((i++))
    done <<< "${backups}"
    
    echo -n "Select backup to restore (1-$((i-1))) or 0 to cancel: "
    read -r choice
    
    if [[ "${choice}" == "0" ]]; then
        log "Restore cancelled"
        return 0
    fi
    
    local selected_backup
    selected_backup=$(echo "${backups}" | sed -n "${choice}p")
    
    if [[ -z "${selected_backup}" ]]; then
        error "Invalid selection"
        return 1
    fi
    
    warning "This will replace your current nginx configuration with:"
    echo "${selected_backup}"
    echo -n "Are you sure? (y/N): "
    read -r confirm
    
    if [[ "${confirm}" =~ ^[Yy]$ ]]; then
        log "Restoring from ${selected_backup}..."
        
        # Stop nginx
        sudo systemctl stop nginx
        
        # Backup current config before restore
        sudo mv /etc/nginx /etc/nginx.before-restore-$(date +%Y%m%d_%H%M%S)
        
        # Restore backup
        sudo cp -r "${selected_backup}" /etc/nginx
        
        # Test and start
        if sudo nginx -t; then
            sudo systemctl start nginx
            success "Configuration restored and nginx restarted"
        else
            error "Restored configuration has errors"
            return 1
        fi
    else
        log "Restore cancelled"
    fi
}

# Main function
main() {
    local action="${1:-}"
    
    if [[ -z "${action}" ]]; then
        show_usage
        exit 1
    fi
    
    case "${action}" in
        analyze)
            analyze_current_config
            ;;
        additional)
            add_as_additional_site
            ;;
        integrate)
            integrate_into_existing
            ;;
        separate)
            setup_separate_ports
            ;;
        restore)
            restore_from_backup
            ;;
        -h|--help)
            show_usage
            ;;
        *)
            error "Unknown option: ${action}"
            show_usage
            exit 1
            ;;
    esac
}

# Script execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi