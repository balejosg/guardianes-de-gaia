# 🚀 Guardianes de Gaia CI/CD Workflows

This directory contains a comprehensive CI/CD system for the Guardianes de Gaia project, implementing enterprise-grade DevOps practices with advanced deployment strategies.

## 📋 Overview

The CI/CD system provides:
- **Automated Testing**: Unit, integration, and end-to-end testing
- **Security Scanning**: Multi-layer security analysis and vulnerability detection
- **Quality Gates**: Code quality enforcement and compliance checking
- **Multi-Environment Deployment**: Development, staging, and production environments
- **Advanced Deployment Strategies**: Blue-green, canary, and A/B testing deployments
- **Mobile App Store Deployment**: Automated deployment to Google Play Store and Apple App Store
- **Infrastructure as Code**: Kubernetes deployment with Helm charts
- **Monitoring & Observability**: Comprehensive monitoring and alerting system

## 🔧 Workflow Files

### Core CI/CD Pipelines

#### 1. **backend-ci.yml** - 🔄 Backend CI Pipeline
- **Trigger**: Push/PR to main/develop (backend changes)
- **Features**:
  - Multi-Java version testing (17, 21)
  - SpotBugs security analysis
  - OWASP dependency checking
  - SonarQube quality analysis
  - JaCoCo code coverage
  - Integration tests with real services
  - Cucumber BDD tests
  - Performance testing with JMeter

#### 2. **mobile-ci.yml** - 📱 Mobile CI Pipeline
- **Trigger**: Push/PR to main/develop (mobile changes)
- **Features**:
  - Multi-Flutter version testing
  - Cross-platform builds (Android/iOS)
  - Code generation and analysis
  - Widget and unit testing
  - Performance testing
  - Security scanning
  - APK/IPA artifact generation

#### 3. **docker-build.yml** - 🐳 Docker Build & Push
- **Trigger**: Push to main/develop, tags, manual
- **Features**:
  - Multi-architecture builds (AMD64, ARM64)
  - Security scanning with Trivy
  - SBOM generation
  - Image signing and attestation
  - Multi-registry deployment
  - Production image promotion

#### 4. **docker-compose.yml** - 🐳 Docker Compose Stack
- **Trigger**: Docker Compose file changes
- **Features**:
  - Full stack testing
  - Service health validation
  - Performance metrics collection
  - Security configuration scanning
  - Integration testing

### Security & Quality

#### 5. **security-quality-gates.yml** - 🛡️ Security & Quality Gates
- **Trigger**: Push/PR, scheduled daily
- **Features**:
  - CodeQL security analysis
  - Trivy filesystem scanning
  - Semgrep SAST analysis
  - OWASP dependency checking
  - Snyk vulnerability scanning
  - License compliance checking
  - Mobile security analysis
  - Policy compliance validation

### Deployment Strategies

#### 6. **deploy-environments.yml** - 🚀 Multi-Environment Deployment
- **Trigger**: Push to main/develop, manual
- **Features**:
  - Environment-specific deployments
  - Blue-green deployment strategy
  - Automated rollback capabilities
  - Pre/post-deployment validation
  - ECS service management
  - Health monitoring

#### 7. **environment-promotion.yml** - 🔄 Environment Promotion Pipeline
- **Trigger**: Manual workflow dispatch
- **Features**:
  - Controlled environment promotion
  - Configuration validation
  - Comprehensive testing
  - Rollback capabilities
  - Approval workflows

#### 8. **advanced-deployment-strategies.yml** - 🚀 Advanced Deployment Strategies
- **Trigger**: Manual workflow dispatch
- **Features**:
  - Blue-green deployments
  - Canary releases
  - A/B testing
  - Traffic splitting with Istio
  - Automated monitoring and rollback
  - Performance comparison

### Infrastructure & Monitoring

#### 9. **helm-deploy.yml** - ⚓ Helm Deployment
- **Trigger**: Helm chart changes, manual
- **Features**:
  - Helm chart validation
  - Dependency management
  - Environment-specific deployments
  - Security scanning
  - Deployment verification

#### 10. **monitoring-observability.yml** - 📊 Monitoring & Observability
- **Trigger**: Monitoring config changes, manual
- **Features**:
  - Prometheus deployment
  - Grafana dashboard configuration
  - Alert rule management
  - Business metrics setup
  - Monitoring stack validation

### Mobile App Store

#### 11. **mobile-app-store-deploy.yml** - 📱 Mobile App Store Deployment
- **Trigger**: Release events, manual
- **Features**:
  - Google Play Store deployment
  - Apple App Store deployment
  - Fastlane integration
  - Release notes generation
  - Store metadata management
  - Multi-track deployment support

## 🎯 Deployment Strategies

### Traditional Strategies
- **Rolling Updates**: Gradual replacement of instances
- **Blue-Green**: Instant traffic switch between environments
- **Canary**: Gradual traffic migration to new version

### Advanced Strategies
- **A/B Testing**: Feature testing with user segmentation
- **Traffic Splitting**: Istio-based traffic management
- **Automated Rollback**: Failure detection and automatic recovery

## 🔐 Security Features

### Code Security
- **Static Analysis**: CodeQL, Semgrep, SpotBugs
- **Dependency Scanning**: OWASP, Snyk vulnerability detection
- **Container Security**: Trivy scanning, image signing
- **License Compliance**: FOSSA integration

### Deployment Security
- **Secret Management**: Kubernetes secrets and external providers
- **Network Policies**: Kubernetes network security
- **Pod Security**: Security contexts and policies
- **RBAC**: Role-based access control

## 📊 Monitoring & Observability

### Application Metrics
- **Performance**: Response times, throughput, error rates
- **Business**: User engagement, feature adoption
- **Infrastructure**: Resource usage, capacity planning

### Alerting
- **Technical Alerts**: Service health, performance degradation
- **Business Alerts**: User engagement drops, feature failures
- **Security Alerts**: Vulnerability detection, compliance issues

## 🚀 Getting Started

### Prerequisites
- GitHub repository with appropriate secrets configured
- Kubernetes cluster with Helm and Istio (for advanced deployments)
- Docker registry access
- App store developer accounts (for mobile deployment)

### Required Secrets
```bash
# Backend deployment
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
KUBECONFIG_DEV
KUBECONFIG_STAGING
KUBECONFIG_PROD

# Mobile deployment
ANDROID_KEYSTORE_BASE64
ANDROID_KEYSTORE_PASSWORD
ANDROID_KEY_PASSWORD
ANDROID_KEY_ALIAS
GOOGLE_SERVICE_ACCOUNT_JSON

IOS_DISTRIBUTION_CERTIFICATE_BASE64
IOS_PROVISIONING_PROFILE_BASE64
IOS_KEYCHAIN_PASSWORD
IOS_CERTIFICATE_PASSWORD
APPLE_ID
APPLE_APP_SPECIFIC_PASSWORD
APPLE_TEAM_ID

# Security scanning
SONAR_TOKEN
SNYK_TOKEN
FOSSA_API_KEY
SEMGREP_PUBLISH_TOKEN

# Notifications
SLACK_WEBHOOK
SLACK_WEBHOOK_SECURITY
SLACK_WEBHOOK_DEPLOYMENTS
SLACK_WEBHOOK_MONITORING
SLACK_WEBHOOK_RELEASES
```

### Environment Configuration
- **Development**: Auto-deploy from `develop` branch
- **Staging**: Auto-deploy from `main` branch
- **Production**: Manual deployment with approval gates

## 🔧 Customization

### Adding New Environments
1. Create new values file in `helm/values-<environment>.yaml`
2. Add environment-specific secrets
3. Update deployment workflows
4. Configure monitoring dashboards

### Modifying Deployment Strategies
1. Update `advanced-deployment-strategies.yml`
2. Configure Istio VirtualServices
3. Add monitoring and alerting
4. Test rollback procedures

### Adding New Security Scans
1. Add scanner configuration to `security-quality-gates.yml`
2. Configure SARIF upload
3. Add quality gate thresholds
4. Update notification channels

## 🐛 Troubleshooting

### Common Issues
- **Test Failures**: Check test logs and fix underlying issues
- **Deployment Failures**: Verify secrets and cluster access
- **Security Scan Failures**: Review and address security findings
- **Mobile Build Failures**: Check signing certificates and provisioning profiles

### Debugging Steps
1. Check workflow logs in GitHub Actions
2. Verify secret configuration
3. Test cluster connectivity
4. Validate Helm chart syntax
5. Check resource quotas and limits

## 📚 Documentation

- **Project Overview**: `/docs/PROYECTO.md`
- **Vertical Slicing**: `/docs/VERTICAL_SLICING_STRATEGY.md`
- **Technical Stack**: `/docs/TECH_STACK.md`
- **Development Rules**: `/CLAUDE.md`

## 🤝 Contributing

1. Follow the vertical slicing strategy
2. Write tests first (TDD approach)
3. Ensure all quality gates pass
4. Update documentation
5. Test deployment workflows

## 📄 License

This project is licensed under the MIT License. See the LICENSE file for details.

---

**Note**: This CI/CD system is designed for the Guardianes de Gaia project and follows enterprise-grade DevOps practices. All deployments require proper testing and security validation before reaching production.