#!/bin/bash
# Guardianes de Gaia - SSL/TLS Certificate Setup with Let's Encrypt
# This script sets up SSL certificates for all three environments

# Check if we're running in bash
if [ -z "${BASH_VERSION:-}" ]; then
    echo "This script requires bash. Please run with: bash $0 $*"
    exit 1
fi

set -euo pipefail

# Configuration
DOMAINS=(
    "dev-guardianes.duckdns.org"
    "stg-guardianes.duckdns.org"
    "guardianes.duckdns.org"
)

EMAIL="${LETSENCRYPT_EMAIL:-admin@guardianes-de-gaia.com}"
WEBROOT_PATH="/var/www/certbot"
NGINX_CONF_PATH="/etc/nginx/sites-available/guardianes"
CERTBOT_PATH="/etc/letsencrypt"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
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

# Check if running as root
check_root() {
    if [[ $EUID -ne 0 ]]; then
        error "This script must be run as root"
        exit 1
    fi
}

# Install required packages
install_dependencies() {
    log "Installing required packages..."
    
    if command -v apt-get >/dev/null 2>&1; then
        apt-get update
        apt-get install -y certbot python3-certbot-nginx nginx
    elif command -v yum >/dev/null 2>&1; then
        yum install -y certbot python3-certbot-nginx nginx
    elif command -v dnf >/dev/null 2>&1; then
        dnf install -y certbot python3-certbot-nginx nginx
    else
        error "Package manager not supported. Please install certbot and nginx manually."
        exit 1
    fi
    
    success "Dependencies installed successfully"
}

# Create webroot directory for ACME challenge
setup_webroot() {
    log "Setting up webroot directory..."
    
    mkdir -p "${WEBROOT_PATH}"
    chown -R www-data:www-data "${WEBROOT_PATH}" 2>/dev/null || chown -R nginx:nginx "${WEBROOT_PATH}" 2>/dev/null || true
    chmod -R 755 "${WEBROOT_PATH}"
    
    success "Webroot directory created: ${WEBROOT_PATH}"
}

# Create temporary nginx configuration for ACME challenge
create_temp_nginx_config() {
    log "Creating temporary nginx configuration for ACME challenge..."
    
    cat > /etc/nginx/sites-available/temp-acme << 'EOF'
server {
    listen 80;
    listen [::]:80;
    server_name dev-guardianes.duckdns.org stg-guardianes.duckdns.org guardianes.duckdns.org;
    
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
        try_files $uri =404;
    }
    
    location / {
        return 301 https://$server_name$request_uri;
    }
}
EOF
    
    # Enable temporary configuration
    ln -sf /etc/nginx/sites-available/temp-acme /etc/nginx/sites-enabled/temp-acme
    
    # Disable default nginx configuration if it exists
    rm -f /etc/nginx/sites-enabled/default
    
    # Test and reload nginx
    nginx -t && systemctl reload nginx
    
    success "Temporary nginx configuration created and active"
}

# Obtain SSL certificates for all domains
obtain_certificates() {
    log "Obtaining SSL certificates for all domains..."
    
    for domain in "${DOMAINS[@]}"; do
        log "Processing domain: ${domain}"
        
        # Check if certificate already exists
        if [[ -f "${CERTBOT_PATH}/live/${domain}/fullchain.pem" ]]; then
            warning "Certificate for ${domain} already exists. Skipping..."
            continue
        fi
        
        # Obtain certificate
        log "Obtaining certificate for ${domain}..."
        if certbot certonly \
            --webroot \
            --webroot-path="${WEBROOT_PATH}" \
            --email "${EMAIL}" \
            --agree-tos \
            --no-eff-email \
            --force-renewal \
            -d "${domain}"; then
            success "Certificate obtained for ${domain}"
        else
            error "Failed to obtain certificate for ${domain}"
            exit 1
        fi
    done
    
    success "All certificates obtained successfully"
}

# Create production nginx configuration with SSL
setup_production_nginx() {
    log "Setting up production nginx configuration with SSL..."
    
    # Remove temporary configuration
    rm -f /etc/nginx/sites-enabled/temp-acme
    
    # Copy the main configuration file
    if [[ -f "/home/run0/guardianes-de-gaia/deploy/nginx/guardianes.conf" ]]; then
        cp "/home/run0/guardianes-de-gaia/deploy/nginx/guardianes.conf" "${NGINX_CONF_PATH}"
    else
        error "Main nginx configuration file not found"
        exit 1
    fi
    
    # Enable the configuration
    ln -sf "${NGINX_CONF_PATH}" /etc/nginx/sites-enabled/guardianes
    
    # Create default SSL certificate for catch-all server
    create_default_ssl_cert
    
    # Test configuration
    if nginx -t; then
        systemctl reload nginx
        success "Production nginx configuration active with SSL"
    else
        error "Nginx configuration test failed"
        exit 1
    fi
}

# Create self-signed certificate for default server block
create_default_ssl_cert() {
    log "Creating default SSL certificate for catch-all server..."
    
    mkdir -p /etc/nginx/ssl
    
    if [[ ! -f /etc/nginx/ssl/default.crt ]]; then
        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout /etc/nginx/ssl/default.key \
            -out /etc/nginx/ssl/default.crt \
            -subj "/C=ES/ST=Madrid/L=Madrid/O=Guardianes/OU=IT/CN=default.local"
        
        success "Default SSL certificate created"
    else
        log "Default SSL certificate already exists"
    fi
}

# Setup automatic certificate renewal
setup_auto_renewal() {
    log "Setting up automatic certificate renewal..."
    
    # Create renewal script
    cat > /etc/cron.d/certbot-renewal << 'EOF'
# Automatic certificate renewal for Guardianes de Gaia
SHELL=/bin/sh
PATH=/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin

# Run twice daily at 3:17 AM and 3:17 PM
17 3,15 * * * root certbot renew --quiet --deploy-hook "systemctl reload nginx"
EOF
    
    # Make sure cron service is enabled
    systemctl enable cron 2>/dev/null || systemctl enable crond 2>/dev/null || true
    systemctl start cron 2>/dev/null || systemctl start crond 2>/dev/null || true
    
    success "Automatic renewal configured (runs twice daily)"
}

# Test certificate validity
test_certificates() {
    log "Testing certificate validity..."
    
    for domain in "${DOMAINS[@]}"; do
        log "Testing certificate for ${domain}..."
        
        if openssl x509 -in "${CERTBOT_PATH}/live/${domain}/fullchain.pem" -text -noout >/dev/null 2>&1; then
            # Get certificate expiration date
            exp_date=$(openssl x509 -in "${CERTBOT_PATH}/live/${domain}/fullchain.pem" -noout -enddate | cut -d= -f2)
            success "Certificate for ${domain} is valid (expires: ${exp_date})"
        else
            error "Certificate for ${domain} is invalid"
            exit 1
        fi
    done
    
    success "All certificates are valid"
}

# Setup DH parameters for better security
setup_dhparam() {
    log "Setting up DH parameters for enhanced security..."
    
    if [[ ! -f /etc/nginx/ssl/dhparam.pem ]]; then
        log "Generating DH parameters (this may take a few minutes)..."
        openssl dhparam -out /etc/nginx/ssl/dhparam.pem 2048
        success "DH parameters generated"
    else
        log "DH parameters already exist"
    fi
}

# Create backup of certificates
backup_certificates() {
    log "Creating backup of certificates..."
    
    backup_dir="/home/run0/guardianes-de-gaia/backup/ssl/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "${backup_dir}"
    
    if [[ -d "${CERTBOT_PATH}" ]]; then
        cp -r "${CERTBOT_PATH}" "${backup_dir}/"
        success "Certificates backed up to: ${backup_dir}"
    else
        warning "No certificates found to backup"
    fi
}

# Display certificate information
show_certificate_info() {
    log "Certificate Information Summary:"
    echo
    
    for domain in "${DOMAINS[@]}"; do
        if [[ -f "${CERTBOT_PATH}/live/${domain}/fullchain.pem" ]]; then
            echo -e "${GREEN}Domain: ${domain}${NC}"
            echo -e "  Certificate: ${CERTBOT_PATH}/live/${domain}/fullchain.pem"
            echo -e "  Private Key: ${CERTBOT_PATH}/live/${domain}/privkey.pem"
            
            # Get certificate details
            exp_date=$(openssl x509 -in "${CERTBOT_PATH}/live/${domain}/fullchain.pem" -noout -enddate | cut -d= -f2)
            echo -e "  Expires: ${exp_date}"
            echo
        else
            echo -e "${RED}Domain: ${domain} - No certificate found${NC}"
            echo
        fi
    done
}

# Main execution function
main() {
    log "Starting SSL/TLS certificate setup for Guardianes de Gaia"
    
    # Validate input
    if [[ -z "${EMAIL}" ]]; then
        error "Email address is required. Set LETSENCRYPT_EMAIL environment variable."
        exit 1
    fi
    
    # Check prerequisites
    check_root
    
    # Install dependencies
    install_dependencies
    
    # Setup process
    setup_webroot
    create_temp_nginx_config
    obtain_certificates
    setup_dhparam
    setup_production_nginx
    setup_auto_renewal
    test_certificates
    backup_certificates
    
    # Show results
    show_certificate_info
    
    success "SSL/TLS setup completed successfully!"
    log "Next steps:"
    log "  1. Verify that your DuckDNS domains are pointing to this server"
    log "  2. Test HTTPS access to all three environments"
    log "  3. Monitor certificate auto-renewal in system logs"
    echo
    log "Certificate renewal is automated and will run twice daily"
    log "Manual renewal: certbot renew --dry-run"
}

# Script execution
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi