#!/bin/bash
# =============================================================================
# Generate Prometheus Configuration from Environment Variables
# =============================================================================
# 
# This script generates a secure prometheus.yml configuration file from
# environment variables to avoid hardcoded credentials.
#
# Usage: ./generate_prometheus_config.sh
# Required environment variables: PROMETHEUS_USERNAME, PROMETHEUS_PASSWORD
#
# =============================================================================

set -euo pipefail

# Configuration file paths
TEMPLATE_FILE="docker/prometheus/prometheus.yml.template"
OUTPUT_FILE="docker/prometheus/prometheus.yml"

# Check required environment variables
if [[ -z "${PROMETHEUS_USERNAME:-}" ]]; then
    echo "ERROR: PROMETHEUS_USERNAME environment variable is required"
    exit 1
fi

if [[ -z "${PROMETHEUS_PASSWORD:-}" ]]; then
    echo "ERROR: PROMETHEUS_PASSWORD environment variable is required"
    exit 1
fi

# Create template if it doesn't exist
if [[ ! -f "$TEMPLATE_FILE" ]]; then
    echo "Creating prometheus configuration template..."
    cat > "$TEMPLATE_FILE" << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'guardianes-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']
    basic_auth:
      username: '${PROMETHEUS_USERNAME}'
      password: '${PROMETHEUS_PASSWORD}'

  - job_name: 'mysql-exporter'
    static_configs:
      - targets: ['mysql:3306']

  - job_name: 'redis-exporter'
    static_configs:
      - targets: ['redis:6379']

  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq:15692']
EOF
fi

# Generate the configuration file
echo "Generating prometheus.yml with secure credentials..."
envsubst < "$TEMPLATE_FILE" > "$OUTPUT_FILE"

echo "✅ Prometheus configuration generated successfully"
echo "📁 Output: $OUTPUT_FILE"

# Validate the generated file
if [[ -f "$OUTPUT_FILE" ]]; then
    echo "🔍 Validating generated configuration..."
    
    # Check that credentials were substituted (not containing literal ${...})
    if grep -q '${PROMETHEUS_' "$OUTPUT_FILE"; then
        echo "❌ ERROR: Environment variable substitution failed"
        echo "   Found unreplaced variables in $OUTPUT_FILE"
        exit 1
    fi
    
    echo "✅ Configuration validation passed"
else
    echo "❌ ERROR: Failed to generate $OUTPUT_FILE"
    exit 1
fi