#!/bin/sh
# OMV/Debian Wrapper Script for Guardianes de Gaia
# This ensures scripts run with bash on systems where sh is default

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Check if bash is available
if ! command -v bash >/dev/null 2>&1; then
    echo "${RED}Error: bash is required but not found.${NC}"
    echo "Please install bash: sudo apt update && sudo apt install bash"
    exit 1
fi

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Show usage if no arguments
if [ $# -eq 0 ]; then
    echo "${BLUE}OMV/Debian Wrapper for Guardianes de Gaia${NC}"
    echo
    echo "Usage: $0 [SCRIPT] [ARGUMENTS...]"
    echo
    echo "Available scripts:"
    echo "  nginx-analyze     - Analyze current nginx configuration"
    echo "  nginx-additional  - Add Guardianes as additional nginx site"
    echo "  nginx-integrate   - Integrate with existing nginx config"
    echo "  nginx-separate    - Use separate ports for Guardianes"
    echo "  deploy [cmd] [env] - Deployment management"
    echo "  monitor [cmd]     - Health monitoring"
    echo "  ssl-setup         - Setup SSL certificates"
    echo "  test-integration  - Test nginx integration"
    echo "  backup-nginx      - Backup current nginx configuration"
    echo
    echo "Examples:"
    echo "  $0 nginx-analyze"
    echo "  $0 nginx-additional"
    echo "  $0 deploy init development"
    echo "  $0 monitor check"
    echo "  $0 ssl-setup"
    exit 0
fi

# Parse command
COMMAND="$1"
shift

case "$COMMAND" in
    nginx-analyze)
        echo "${BLUE}Running nginx analysis...${NC}"
        bash "$SCRIPT_DIR/nginx-integration.sh" analyze "$@"
        ;;
    nginx-additional)
        echo "${BLUE}Adding Guardianes as additional nginx site...${NC}"
        bash "$SCRIPT_DIR/nginx-integration.sh" additional "$@"
        ;;
    nginx-integrate)
        echo "${BLUE}Integrating with existing nginx configuration...${NC}"
        bash "$SCRIPT_DIR/nginx-integration.sh" integrate "$@"
        ;;
    nginx-separate)
        echo "${BLUE}Setting up separate ports for Guardianes...${NC}"
        bash "$SCRIPT_DIR/nginx-integration.sh" separate "$@"
        ;;
    deploy)
        echo "${BLUE}Running deployment script...${NC}"
        bash "$SCRIPT_DIR/deploy.sh" "$@"
        ;;
    monitor)
        echo "${BLUE}Running monitoring script...${NC}"
        bash "$SCRIPT_DIR/monitor.sh" "$@"
        ;;
    ssl-setup)
        echo "${BLUE}Running SSL setup script...${NC}"
        if [ "$(id -u)" -ne 0 ]; then
            echo "${YELLOW}SSL setup requires root privileges. Running with sudo...${NC}"
            sudo bash "$SCRIPT_DIR/../ssl/setup-ssl.sh" "$@"
        else
            bash "$SCRIPT_DIR/../ssl/setup-ssl.sh" "$@"
        fi
        ;;
    test-integration)
        echo "${BLUE}Testing nginx integration...${NC}"
        bash "$SCRIPT_DIR/test-integration.sh" "$@"
        ;;
    backup-nginx)
        echo "${BLUE}Backing up nginx configuration...${NC}"
        bash "$SCRIPT_DIR/backup-nginx.sh" "$@"
        ;;
    *)
        echo "${RED}Unknown command: $COMMAND${NC}"
        echo "Run '$0' without arguments to see available commands."
        exit 1
        ;;
esac