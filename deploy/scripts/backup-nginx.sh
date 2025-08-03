#!/bin/bash
# Simple nginx backup script that avoids recursion issues

# Check if we're running in bash
if [ -z "${BASH_VERSION:-}" ]; then
    echo "This script requires bash. Please run with: bash $0 $*"
    exit 1
fi

set -euo pipefail

# Create backup directory
BACKUP_DIR="/etc/nginx/backup-$(date +%Y%m%d_%H%M%S)"
echo "Creating backup in: $BACKUP_DIR"

sudo mkdir -p "$BACKUP_DIR"

# Backup main config file
if [ -f /etc/nginx/nginx.conf ]; then
    sudo cp /etc/nginx/nginx.conf "$BACKUP_DIR/"
    echo "✓ Backed up nginx.conf"
fi

# Backup sites-available directory
if [ -d /etc/nginx/sites-available ]; then
    sudo cp -r /etc/nginx/sites-available "$BACKUP_DIR/"
    echo "✓ Backed up sites-available"
fi

# Backup sites-enabled directory
if [ -d /etc/nginx/sites-enabled ]; then
    sudo cp -r /etc/nginx/sites-enabled "$BACKUP_DIR/"
    echo "✓ Backed up sites-enabled"
fi

# Backup conf.d directory if it exists
if [ -d /etc/nginx/conf.d ]; then
    sudo cp -r /etc/nginx/conf.d "$BACKUP_DIR/"
    echo "✓ Backed up conf.d"
fi

# Backup modules-enabled directory if it exists
if [ -d /etc/nginx/modules-enabled ]; then
    sudo cp -r /etc/nginx/modules-enabled "$BACKUP_DIR/"
    echo "✓ Backed up modules-enabled"
fi

# Backup snippets directory if it exists
if [ -d /etc/nginx/snippets ]; then
    sudo cp -r /etc/nginx/snippets "$BACKUP_DIR/"
    echo "✓ Backed up snippets"
fi

echo "✅ Backup completed: $BACKUP_DIR"
echo "$BACKUP_DIR"