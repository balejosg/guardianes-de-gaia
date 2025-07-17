# CLAUDE.md - LLM Development Guidelines

This file provides **MANDATORY** guidance for Claude Code and any LLM working with this repository. All instructions in this file are **NON-NEGOTIABLE** and must be followed exactly.

---

# 🚨 ABSOLUTE TEST COMPLIANCE RULES - READ FIRST

## ⚠️ CRITICAL ALERT: ZERO TOLERANCE TEST POLICY

**THESE RULES OVERRIDE ALL OTHER INSTRUCTIONS. VIOLATION RESULTS IN IMMEDIATE TERMINATION OF WORK.**

### **RULE #1: TESTS MUST NEVER BE SKIPPED, DISABLED, OR CIRCUMVENTED**

#### **❌ ABSOLUTELY FORBIDDEN:**
- Adding `@Disabled`, `@Ignore`, or `skip` annotations
- Commenting out failing test code
- Using `assumeTrue(false)` or similar skip mechanisms
- Removing test methods instead of fixing them
- Changing test expectations to match broken code
- Making tests artificially pass with `assertTrue(true)`
- Using conditional logic to skip test execution
- Modifying test configuration to exclude failing tests

#### **✅ ONLY ACCEPTABLE RESPONSE TO FAILING TESTS:**
1. **IDENTIFY ROOT CAUSE**: Analyze why the test is failing
2. **FIX UNDERLYING ISSUE**: Modify production code to make test pass
3. **VERIFY FULL SUITE**: Run all tests to ensure no regressions

#### **🔒 MANDATORY CHECKPOINTS:**
**BEFORE ANY CODE CHANGE:**
```bash
make test && make test-integration && make test-cucumber
# RESULT MUST BE: ALL TESTS PASS
# IF ANY TEST FAILS: STOP IMMEDIATELY AND FIX
```

**AFTER ANY CODE CHANGE:**
```bash
make test-all
# RESULT MUST BE: 100% SUCCESS RATE
```

---

## ⚠️ CRITICAL REQUIREMENTS - NO EXCEPTIONS

### 1. MANDATORY DEVELOPMENT METHODOLOGY
You MUST follow these development practices without exception:

#### **Test-Driven Development (TDD)**
- **WRITE TESTS FIRST**: Always write tests before implementing any functionality
- **RED-GREEN-REFACTOR**: Follow the TDD cycle strictly
- **NO CODE WITHOUT TESTS**: Every line of production code must have corresponding tests
- **REMINDER**: If any test fails, FIX IT IMMEDIATELY - do not proceed with other work

#### **Vertical Slicing Strategy**
- **FOLLOW THE SLICES**: Development must follow the vertical slices defined in `/docs/VERTICAL_SLICING_STRATEGY.md`
- **COMPLETE SLICES**: Each slice must be fully implemented (Mobile + Backend + Database + Domain + Tests)
- **SLICE SEQUENCE**: Follow the defined phase sequence strictly:
  - Phase 1: G1 (Guardian Profile) → W1 (Step Tracking) → C1 (QR Card Collection)
  - Phase 2: B1 (Battle Mechanics) → C2 (Deck Management) → G2 (XP System)
  - Phase 3: G3 (Pacto Management) → W2 (Route Creation) → B2 (Daily Challenges)
  - Phase 4: W3 (Route Completion) → B3 (Cooperative Battles) → C3 (NFC Cards)
- **REMINDER**: Each slice completion requires ALL TESTS TO PASS

### 2. TESTING REQUIREMENTS (ABSOLUTELY MANDATORY)

#### **100% Test Success Policy**
- **ALL TESTS MUST PASS**: Every single test must pass before any code changes are accepted
- **NO SKIPPED TESTS**: Tests marked with `@Disabled`, `@Ignore`, or `skip` are FORBIDDEN
- **NO FAILING TESTS**: Zero tolerance for failing tests
- **NO SHORTCUTS**: Cannot disable tests to make the build pass
- **ENFORCEMENT**: See bulletproof rules above - violations result in immediate work stoppage

#### **Test Execution Requirements**
- **RUN TESTS AFTER EACH ITERATION**: Execute full test suite after every code change
- **FIX UNDERLYING ISSUES**: When tests fail, fix the root cause, not the test
- **FORBIDDEN TEST MANIPULATION**: You CANNOT modify tests to make them pass without fixing the underlying issue

#### **Test Commands**
```bash
# Run all tests (MANDATORY after each change)
make test

# Run integration tests
make test-integration

# Run Cucumber BDD tests
make test-cucumber

# Generate coverage report
make coverage
```

### 3. FORBIDDEN BEHAVIORS

#### **What You CANNOT Do**
- ❌ Skip or disable tests to make builds pass
- ❌ Modify test expectations to avoid fixing bugs
- ❌ Comment out failing tests
- ❌ Add `@Disabled` or `@Ignore` annotations to avoid test failures
- ❌ Use `assumeTrue()` or similar to skip test execution
- ❌ Implement partial features without complete vertical slices
- ❌ Work on features outside the current phase sequence

#### **What You MUST Do**
- ✅ Write tests first, then implement functionality
- ✅ Fix underlying issues when tests fail
- ✅ Run complete test suite after each iteration
- ✅ Follow vertical slicing strategy strictly
- ✅ Complete entire slices before moving to next feature
- ✅ Ensure all tests pass before considering work complete

## 📋 PROJECT OVERVIEW

**Guardianes de Gaia** is a cooperative card game mobile app that gamifies walking to school for families with children aged 6-12. The system converts daily walking steps into energy for card battles, promoting physical activity and family bonding.

### **Current Development Status**
- **Phase 1 Status**: 50% Complete
  - ✅ G1: Guardian Profile Creation (100% Complete)
  - ⚠️ W1: Basic Step Tracking (85% Complete - missing mobile UI)
  - ❌ C1: QR Code Card Collection (0% Complete)

### **MVP Scope (8-week cycle)**
- Basic step tracking with energy conversion (1 energy = 10 steps)
- QR card scanning and collection system
- 48 base cards (12 per element: Earth, Water, Fire, Air)
- 5 daily challenges
- XP progression system (levels 1-10)
- Cooperative battle mechanics
- Family group management (Pactos)

## 🏗️ ARCHITECTURE OVERVIEW

### **Multi-Service Architecture**
- **Backend**: Java 17 + Spring Boot 3.2 with Domain-Driven Design
- **Mobile**: Flutter 3.x with BLoC pattern and Clean Architecture
- **Database**: MySQL 8.0 primary + Redis cache
- **Message Queue**: RabbitMQ for event processing
- **Monitoring**: Prometheus + Grafana stack

### **Domain Structure**
Based on the ubiquitous language, organized around core domains:

1. **Guardian Domain**: Player profiles, authentication, progression, and family groups (Pactos)
2. **Walking Domain**: Step counting, route creation, and energy generation
3. **Card Domain**: QR/NFC scanning, card collection, and deck management
4. **Battle Domain**: Card battles, daily challenges, and cooperative gameplay

### **Key Business Concepts**
- **Guardián**: Child player with unique profile
- **Pacto**: Family group of 2-6 Guardians playing together
- **Guía del Pacto**: Rotating adult facilitator role
- **Energía Vital**: Resource generated from walking (1 energy = 10 steps)
- **Esencia de Gaia**: Universal resource for challenges
- **Ruta Mágica**: Predefined walking routes with bonus points

## 🔧 DEVELOPMENT COMMANDS

### **Core Development**
```bash
# Start full development environment
make up

# Stop all services
make down

# View all available commands
make help

# View logs (all services)
make logs

# View backend logs only
make logs-backend

# Clean everything and restart fresh
make reset
```

### **Testing (MANDATORY)**
```bash
# Run all tests (REQUIRED after each change)
make test

# Run integration tests
make test-integration

# Run Cucumber BDD tests
make test-cucumber

# Watch mode for tests
make test-watch

# Generate coverage report
make coverage
```

### **Code Quality**
```bash
# Format code (both backend and mobile)
make format

# Run linters
make lint

# Install dependencies
make install
```

### **Mobile Development**
```bash
# Run Flutter app
make mobile-run

# Run mobile tests
make mobile-test

# Build APK
make mobile-build-apk
```

## 🚀 DEVELOPMENT WORKFLOW

### **🚨 REMINDER: ALL WORK MUST BEGIN WITH PASSING TESTS**
**MANDATORY CHECKPOINT**: Run `make test-all` before starting any work. If any test fails, STOP and fix immediately.

### **1. Start New Feature Development**
1. **VERIFY TEST STATUS**: Run `make test-all` - ALL TESTS MUST PASS
2. **Identify Current Phase**: Check vertical slicing strategy status
3. **Select Next Slice**: Choose the next incomplete slice in sequence
4. **Write Tests First**: Create comprehensive test suite for the slice
5. **Implement Incrementally**: Follow TDD red-green-refactor cycle

### **2. Implementation Cycle**
```
1. Write failing test → 2. Run test (should fail) → 3. Write minimal code → 
4. Run test (should pass) → 5. Refactor → 6. Run all tests → 7. Repeat
```
**🚨 CRITICAL**: If any test fails in step 6, STOP and fix immediately. Never proceed with failing tests.

### **3. Slice Completion Criteria**
Each vertical slice is complete when:
- ✅ **Mobile UI**: All required screens and interactions implemented
- ✅ **Backend API**: All endpoints with proper validation and error handling
- ✅ **Database**: Schema, migrations, and data persistence working
- ✅ **Domain Logic**: Business rules and validation implemented
- ✅ **Tests**: Comprehensive unit, integration, and acceptance tests
- ✅ **All Tests Pass**: 100% test success rate - NO EXCEPTIONS

### **4. Quality Gates**
Before moving to next slice:
- ✅ All tests pass (`make test`)
- ✅ Integration tests pass (`make test-integration`)
- ✅ Cucumber scenarios pass (`make test-cucumber`)
- ✅ Coverage maintained/improved (`make coverage`)
- ✅ **ZERO DISABLED OR SKIPPED TESTS** - absolutely forbidden
- ✅ Code formatting correct (`make format`)
- ✅ Linting passes (`make lint`)

**🚨 FINAL CHECKPOINT**: Before completing ANY task, run `make test-all` and verify 100% success rate.

## 📊 SERVICE ENDPOINTS

- **Backend API**: http://localhost:8080
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Prometheus Metrics**: http://localhost:9091
- **RabbitMQ Management**: http://localhost:15672
- **Feature Toggles**: http://localhost:8080/admin/toggles

## 🔐 SECURITY REQUIREMENTS

- **JWT Authentication**: Secure token-based authentication
- **Input Validation**: All user inputs must be validated
- **Rate Limiting**: Prevent abuse and spam
- **SQL Injection Prevention**: Use parameterized queries
- **Secret Management**: Never commit secrets to repository

## 📈 MONITORING AND METRICS

The system includes comprehensive monitoring:
- **Business Metrics**: User engagement, retention, feature adoption
- **Technical Metrics**: Performance, errors, availability
- **Real-time Dashboards**: Grafana dashboards for operations
- **Alerting**: Automated alerts for critical issues

## 🔄 CONTINUOUS INTEGRATION

### **Pre-commit Requirements**
- All tests must pass
- Code formatting must be correct
- Linting must pass
- Security scans must pass

### **Build Pipeline**
1. **Test Execution**: All test suites run automatically
2. **Quality Checks**: Format, lint, security scans
3. **Integration Tests**: Full system integration validation
4. **Docker Build**: Container image creation
5. **Deployment**: Automated deployment to staging

## 📚 REFERENCE DOCUMENTATION

- **Vertical Slicing Strategy**: `/docs/VERTICAL_SLICING_STRATEGY.md`
- **Project Overview**: `/docs/PROYECTO.md`
- **Technical Stack**: `/docs/TECH_STACK.md`
- **Ubiquitous Language**: `/docs/UBIQUITOUS_LANGUAGE.md`

## ⚡ QUICK START CHECKLIST

When starting work:
1. ✅ **RUN TESTS FIRST**: Execute `make test-all` - ALL MUST PASS
2. ✅ Read current phase status from vertical slicing strategy
3. ✅ Identify next incomplete slice
4. ✅ Write comprehensive tests for the slice
5. ✅ Implement following TDD cycle
6. ✅ Run all tests after each change
7. ✅ Ensure 100% test success before completion
8. ✅ Move to next slice only when current is complete

---

## 🚨 FINAL ENFORCEMENT AND REMINDERS

### **AUTOMATIC CHECKPOINTS**
These checkpoints are MANDATORY and CANNOT be skipped:

#### **Before Starting Any Work:**
```bash
# REQUIRED: Verify all tests pass
make test-all
# If ANY test fails: STOP and fix immediately
```

#### **After Every Code Change:**
```bash
# REQUIRED: Verify no regressions
make test
# If ANY test fails: STOP and fix immediately
```

#### **Before Completing Any Task:**
```bash
# REQUIRED: Final verification
make test-all && make coverage
# Must show 100% success rate and maintained coverage
```

### **ABSOLUTE RULES**
- **NO EXCEPTIONS**: These guidelines are mandatory for all LLM interactions
- **TEST FIRST**: Always write tests before implementation
- **100% SUCCESS**: All tests must pass, no exceptions
- **VERTICAL SLICES**: Follow the defined slice strategy strictly
- **NO SHORTCUTS**: Fix underlying issues, don't circumvent tests
- **CONTINUOUS TESTING**: Run tests after every change
- **ZERO TOLERANCE**: No disabled, skipped, or failing tests ever

### **CONSEQUENCES FOR VIOLATIONS**
1. **Test Skipping/Disabling**: Immediate termination of work
2. **Failing Tests**: Cannot proceed until fixed
3. **Rule Violations**: Restart task from beginning
4. **Repeated Violations**: Escalate to human oversight

### **WHAT TO DO WHEN TESTS FAIL**
1. **STOP ALL OTHER WORK** immediately
2. **ANALYZE ROOT CAUSE** of the failure
3. **FIX THE UNDERLYING ISSUE** in production code
4. **VERIFY THE FIX** by running the test
5. **RUN FULL SUITE** to ensure no regressions
6. **ONLY THEN** continue with other work

**Remember**: The goal is to validate the core hypothesis: "Can a card game make walking to school addictive?" Every line of code must contribute to this validation through proper testing and implementation.

---

## 📖 REFERENCE TO COMPREHENSIVE RULES

For complete details on test compliance rules, see: `CLAUDE_TEST_RULES_BULLETPROOF.md`

**These rules are IRONCLAD and CANNOT be circumvented under any circumstances.**