# Guardianes de Gaia - Multi-Environment Deployment

This directory contains the complete multi-environment deployment configuration for hosting development, staging, and production environments on a home server using DuckDNS domains.

## 🏗️ Architecture Overview

The deployment supports three environments:

- **Development**: `dev-guardianes.duckdns.org` → `localhost:8080`
- **Staging**: `stg-guardianes.duckdns.org` → `localhost:8081`
- **Production**: `guardianes.duckdns.org` → `localhost:8082`

Each environment runs in isolated Docker containers with environment-specific configurations, resource limits, and security settings.

## 📁 Directory Structure

```
deploy/
├── nginx/
│   └── guardianes.conf              # Main nginx reverse proxy config
├── scripts/
│   ├── deploy.sh                    # Main deployment script
│   └── monitor.sh                   # Health monitoring script
├── ssl/
│   └── setup-ssl.sh                 # SSL certificate setup
├── docker-compose.staging.yml       # Staging environment config
├── docker-compose.production.yml    # Production environment config
├── .env.staging                     # Staging environment variables
├── .env.production                  # Production environment variables
└── README.md                        # This file
```

## 🚀 Quick Start

### 1. Prerequisites

- Docker and Docker Compose installed
- Nginx installed and configured
- Domain names configured in DuckDNS
- Root access for SSL certificate setup

### 2. Initial Setup

```bash
# Clone the repository
cd /path/to/guardianes-de-gaia

# Set up SSL certificates (run as root)
sudo ./deploy/ssl/setup-ssl.sh

# Configure environment variables
cp deploy/.env.staging.example deploy/.env.staging
cp deploy/.env.production.example deploy/.env.production

# Edit the files with your actual credentials
nano deploy/.env.staging
nano deploy/.env.production
```

### 3. Deploy Environments

```bash
# Initialize and deploy development (uses existing docker-compose.yml)
./deploy/scripts/deploy.sh init development

# Initialize and deploy staging
./deploy/scripts/deploy.sh init staging

# Initialize and deploy production
./deploy/scripts/deploy.sh init production
```

### 4. Configure Nginx

```bash
# Copy nginx configuration
sudo cp deploy/nginx/guardianes.conf /etc/nginx/sites-available/
sudo ln -s /etc/nginx/sites-available/guardianes.conf /etc/nginx/sites-enabled/

# Test and reload nginx
sudo nginx -t
sudo systemctl reload nginx
```

## 🛠️ Management Commands

### Deployment Script

The main deployment script (`deploy/scripts/deploy.sh`) provides comprehensive environment management:

```bash
# Deploy specific environment
./deploy/scripts/deploy.sh deploy staging

# Start/stop environments
./deploy/scripts/deploy.sh start production
./deploy/scripts/deploy.sh stop staging

# Check status
./deploy/scripts/deploy.sh status all

# View logs
./deploy/scripts/deploy.sh logs development

# Backup data
./deploy/scripts/deploy.sh backup production

# Update environment (rebuild and redeploy)
./deploy/scripts/deploy.sh update staging --build

# Health check
./deploy/scripts/deploy.sh health all
```

### Monitoring Script

The monitoring script (`deploy/scripts/monitor.sh`) provides health monitoring:

```bash
# Single health check
./deploy/scripts/monitor.sh check

# Continuous monitoring
./deploy/scripts/monitor.sh monitor --interval 60

# Generate health report
./deploy/scripts/monitor.sh report

# Show current status
./deploy/scripts/monitor.sh status
```

## 🌐 Environment Configuration

### Development Environment

- **Domain**: `dev-guardianes.duckdns.org`
- **Port**: `8080`
- **Features**: Full debugging, development tools, relaxed security
- **Docker Compose**: Uses existing `docker-compose.yml`

### Staging Environment

- **Domain**: `stg-guardianes.duckdns.org`
- **Port**: `8081`
- **Features**: Production-like configuration, basic authentication for monitoring
- **Docker Compose**: `deploy/docker-compose.staging.yml`

### Production Environment

- **Domain**: `guardianes.duckdns.org`
- **Port**: `8082`
- **Features**: Maximum security, SSL/TLS, resource optimization, internal monitoring only
- **Docker Compose**: `deploy/docker-compose.production.yml`

## 🔒 SSL/TLS Configuration

The SSL setup script (`deploy/ssl/setup-ssl.sh`) automatically:

1. Installs Certbot and dependencies
2. Obtains Let's Encrypt certificates for all domains
3. Configures automatic renewal
4. Sets up secure nginx configuration with HTTPS

### Manual SSL Commands

```bash
# Setup SSL certificates
sudo ./deploy/ssl/setup-ssl.sh

# Manual certificate renewal
sudo certbot renew --dry-run

# Check certificate status
sudo certbot certificates
```

## 🔧 Port Allocation

| Service | Development | Staging | Production |
|---------|-------------|---------|------------|
| Backend API | 8080 | 8081 | 8082 |
| MySQL | 3306 | 3307 | 3308 |
| Redis | 6379 | 6380 | 6381 |
| RabbitMQ | 5672/15672 | 5673/15673 | 5674/15674 |
| Prometheus | 9091 | 9093 | 9094 (internal) |
| Grafana | 3000 | 3001 | 3002 (internal) |

## 📊 Monitoring & Health Checks

### Health Endpoints

- **Development**: `http://dev-guardianes.duckdns.org/actuator/health`
- **Staging**: `http://stg-guardianes.duckdns.org/actuator/health`
- **Production**: `https://guardianes.duckdns.org/actuator/health`

### Monitoring Features

- Automated health checks for all services
- SSL certificate expiration monitoring
- System resource monitoring (disk, memory)
- Container health verification
- Automated alerting (configurable)
- HTML health reports

### Grafana Dashboards

- **Development**: `http://dev-guardianes.duckdns.org/grafana/`
- **Staging**: `http://stg-guardianes.duckdns.org/grafana/` (password protected)
- **Production**: Internal only (no external access)

## 💾 Backup Strategy

### Automated Backups

- **Schedule**: Daily at 2 AM (production), 3 AM (staging)
- **Retention**: 30 days (production), 7 days (staging)
- **Components**: MySQL databases, Redis data, RabbitMQ configuration

### Manual Backup

```bash
# Backup specific environment
./deploy/scripts/deploy.sh backup production

# Backup all environments
./deploy/scripts/deploy.sh backup all
```

### Backup Locations

- **Local Backups**: `~/guardianes-data/{environment}/backup/`
- **Project Backups**: `./backup/{environment}/`

## 🔐 Security Features

### Production Security

- **HTTPS Only**: All traffic redirected to HTTPS
- **HSTS Headers**: HTTP Strict Transport Security enabled
- **CSP Headers**: Content Security Policy configured
- **Rate Limiting**: API and authentication endpoints protected
- **No External Monitoring**: Grafana/Prometheus internal only
- **Secrets Management**: Environment variables for sensitive data

### Staging Security

- **Basic Authentication**: Monitoring endpoints protected
- **Enhanced Headers**: Security headers for testing
- **SSL/TLS**: Optional HTTPS support

### Development Security

- **Open Access**: Full debugging capabilities
- **Relaxed Policies**: Easy development workflow

## 🗂️ Data Persistence

All environments use persistent volumes:

```bash
# Data directories
~/guardianes-data/
├── development/
├── staging/
└── production/
    ├── mysql/
    ├── redis/
    ├── rabbitmq/
    ├── prometheus/
    └── grafana/
```

## 🔄 Update Procedures

### Rolling Updates

```bash
# Update with zero downtime
./deploy/scripts/deploy.sh update production --build

# Update specific service
docker-compose -f deploy/docker-compose.production.yml up -d --no-deps backend
```

### Emergency Procedures

```bash
# Quick rollback (if images are available)
docker-compose -f deploy/docker-compose.production.yml down
docker-compose -f deploy/docker-compose.production.yml up -d

# Stop all environments
./deploy/scripts/deploy.sh stop all
```

## 🐛 Troubleshooting

### Common Issues

1. **SSL Certificate Issues**
   ```bash
   # Check certificate status
   sudo certbot certificates
   
   # Manual renewal
   sudo certbot renew --force-renewal
   ```

2. **Port Conflicts**
   ```bash
   # Check port usage
   sudo netstat -tlnp | grep :8080
   
   # Kill process using port
   sudo fuser -k 8080/tcp
   ```

3. **Container Health Issues**
   ```bash
   # Check container logs
   ./deploy/scripts/deploy.sh logs production
   
   # Check container status
   docker ps --filter "name=guardianes"
   ```

4. **Database Connection Issues**
   ```bash
   # Test database connection
   docker exec guardianes-mysql-production mysqladmin ping
   
   # Check database logs
   docker logs guardianes-mysql-production
   ```

### Debug Mode

```bash
# Enable verbose logging
./deploy/scripts/deploy.sh deploy staging --verbose

# Dry run (show what would be done)
./deploy/scripts/deploy.sh deploy production --dry-run
```

## 📝 Environment Variables

### Required Variables

Make sure to update these in your `.env` files:

```bash
# Database passwords
PROD_DB_PASSWORD=your_secure_password
STAGING_DB_PASSWORD=your_staging_password

# Redis passwords
PROD_REDIS_PASSWORD=your_redis_password
STAGING_REDIS_PASSWORD=your_staging_redis_password

# JWT secrets (minimum 32 characters)
JWT_SECRET=your_jwt_secret_minimum_32_characters

# DuckDNS token
DUCKDNS_TOKEN=your_duckdns_token

# Email for Let's Encrypt
LETSENCRYPT_EMAIL=your@email.com
```

## 🆘 Support

For issues and support:

1. Check the logs: `./deploy/scripts/deploy.sh logs [environment]`
2. Run health check: `./deploy/scripts/monitor.sh check`
3. Check system resources: `df -h && free -m`
4. Review nginx logs: `sudo tail -f /var/log/nginx/error.log`

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Nginx Configuration Guide](https://nginx.org/en/docs/)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [DuckDNS Setup Guide](https://www.duckdns.org/)