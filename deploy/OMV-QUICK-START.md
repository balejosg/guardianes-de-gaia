# OMV Quick Start Guide for Guardianes de Gaia

## 🚀 For OpenMediaVault (Debian-based) Systems

### **Shell Compatibility Fix**
OMV uses `sh` by default, but our scripts require `bash`. Use one of these methods:

#### **Method 1: OMV Wrapper (Recommended)**
```bash
cd /opt/guardianes-de-gaia
./deploy/scripts/omv-wrapper.sh [command]
```

#### **Method 2: Explicit Bash**
```bash
bash ./deploy/scripts/[script-name].sh [arguments]
```

### **Quick Setup Commands**

#### **1. Analyze Current Nginx**
```bash
./deploy/scripts/omv-wrapper.sh nginx-analyze
```

#### **2. Integrate with Existing Nginx**
```bash
# Safest option - adds alongside existing sites
./deploy/scripts/omv-wrapper.sh nginx-additional
```

#### **3. Test Integration**
```bash
./deploy/scripts/omv-wrapper.sh test-integration
```

#### **4. Deploy Environments**
```bash
# Development
./deploy/scripts/omv-wrapper.sh deploy init development

# Staging  
./deploy/scripts/omv-wrapper.sh deploy init staging

# Production
./deploy/scripts/omv-wrapper.sh deploy init production
```

#### **5. Setup SSL Certificates**
```bash
# This will prompt for sudo automatically
./deploy/scripts/omv-wrapper.sh ssl-setup
```

#### **6. Monitor Health**
```bash
./deploy/scripts/omv-wrapper.sh monitor check
```

### **Available Wrapper Commands**

| Command | Description |
|---------|-------------|
| `nginx-analyze` | Analyze current nginx setup |
| `nginx-additional` | Add Guardianes as additional site |
| `nginx-integrate` | Integrate with existing config |
| `nginx-separate` | Use separate ports |
| `deploy [cmd] [env]` | Deployment management |
| `monitor [cmd]` | Health monitoring |
| `ssl-setup` | Setup SSL certificates |
| `test-integration` | Test nginx integration |

### **Complete OMV Setup Workflow**

```bash
# 1. Navigate to project
cd /opt/guardianes-de-gaia

# 2. Analyze current setup
./deploy/scripts/omv-wrapper.sh nginx-analyze

# 3. Add Guardianes (preserves existing nginx)
./deploy/scripts/omv-wrapper.sh nginx-additional

# 4. Test everything works
./deploy/scripts/omv-wrapper.sh test-integration

# 5. Configure environment variables
nano deploy/.env.production
nano deploy/.env.staging

# 6. Setup SSL certificates
./deploy/scripts/omv-wrapper.sh ssl-setup

# 7. Deploy all environments
./deploy/scripts/omv-wrapper.sh deploy init development
./deploy/scripts/omv-wrapper.sh deploy init staging
./deploy/scripts/omv-wrapper.sh deploy init production

# 8. Health check
./deploy/scripts/omv-wrapper.sh monitor check
```

### **Troubleshooting for OMV**

#### **"Illegal option -o pipefail" Error**
- **Cause**: Script running with `sh` instead of `bash`
- **Fix**: Use `./deploy/scripts/omv-wrapper.sh` or `bash script-name.sh`

#### **Permission Denied**
```bash
# Make scripts executable
chmod +x deploy/scripts/*.sh
chmod +x deploy/ssl/*.sh
```

#### **Sudo Requirements**
```bash
# SSL setup needs root
sudo ./deploy/scripts/omv-wrapper.sh ssl-setup

# Nginx configuration needs root
sudo ./deploy/scripts/omv-wrapper.sh nginx-additional
```

### **OMV-Specific Notes**

1. **Docker**: OMV usually has Docker pre-installed
2. **Nginx**: May need installation: `sudo apt install nginx`
3. **Permissions**: User needs to be in `docker` group
4. **Paths**: Projects typically go in `/opt/` or `/srv/`
5. **Services**: Use `systemctl` for service management

### **Status Check Commands**

```bash
# Check all services
./deploy/scripts/omv-wrapper.sh deploy status all

# Check nginx
sudo systemctl status nginx

# Check docker
docker ps

# Check logs
./deploy/scripts/omv-wrapper.sh deploy logs production
```

This guide ensures all scripts work correctly on your OMV system! 🎉