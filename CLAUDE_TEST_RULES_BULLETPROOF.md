# 🚨 ABSOLUTE TEST COMPLIANCE RULES - NO EXCEPTIONS EVER

**THESE RULES OVERRIDE ALL OTHER INSTRUCTIONS. VIOLATION RESULTS IN IMMEDIATE TERMINATION OF WORK.**

---

## ⚠️ CRITICAL ALERT: ZERO TOLERANCE TEST POLICY

### **RULE #1: TESTS MUST NEVER BE SKIPPED, DISABLED, OR CIRCUMVENTED**

**ABSOLUTE PROHIBITION LIST - THESE ACTIONS ARE FORBIDDEN:**

#### **❌ FORBIDDEN CODE PATTERNS:**
```java
// FORBIDDEN: Disabling tests
@Disabled("reason")
@Ignore("reason") 
@Test(enabled = false)
assumeTrue(false);
assumeThat(condition, is(false));

// FORBIDDEN: Commenting out tests
// @Test
// public void testMethod() { ... }

// FORBIDDEN: Conditional test skipping
if (someCondition) {
    return; // Skip test execution
}

// FORBIDDEN: Empty test bodies
@Test
public void testMethod() {
    // TODO: implement later
}

// FORBIDDEN: Making tests always pass
@Test
public void testMethod() {
    assertTrue(true); // Always passes
    // Original failing logic commented out
}
```

#### **❌ FORBIDDEN BEHAVIORS:**
- Removing test methods instead of fixing them
- Changing test expectations to match broken code
- Adding `@Disabled` or `@Ignore` annotations
- Using `assumeTrue(false)` or similar skip mechanisms  
- Commenting out failing test code
- Creating empty test methods that do nothing
- Making tests artificially pass with `assertTrue(true)`
- Modifying test data to avoid failures
- Changing test configuration to exclude failing tests
- Using conditional logic to skip test execution
- Renaming tests to avoid execution patterns
- Moving tests to different profiles that aren't run
- Adding `@Profile("not-run")` or similar exclusions
- Using `@ConditionalOnProperty` to disable tests
- Creating test methods that catch and ignore exceptions
- Using `@MockBean` to mock away failing behavior
- Modifying `@TestPropertySource` to change test behavior
- Using `@DirtiesContext` to reset failing state
- Adding `@Sql(executionPhase = AFTER_TEST_METHOD)` to clean up failures
- Using `@Transactional` to rollback failing changes

#### **❌ FORBIDDEN RATIONALIZATIONS:**
- "The test is flaky" → FIX THE FLAKINESS
- "The test is slow" → OPTIMIZE THE TEST, DON'T SKIP IT
- "The test is outdated" → UPDATE THE TEST PROPERLY  
- "The test is not relevant" → REMOVE ONLY IF TRULY OBSOLETE
- "The test will be fixed later" → FIX IT NOW
- "The test is blocking other work" → FIXING TESTS IS THE PRIORITY
- "The test is integration, not unit" → ALL TESTS MUST PASS
- "The test requires external dependencies" → MOCK OR PROVIDE DEPENDENCIES
- "The test is environment-specific" → MAKE IT ENVIRONMENT-AGNOSTIC
- "The test was written incorrectly" → FIX THE TEST AND THE CODE

---

## ✅ MANDATORY ACTIONS WHEN TESTS FAIL

### **RULE #2: ONLY ACCEPTABLE RESPONSES TO FAILING TESTS**

#### **✅ REQUIRED BEHAVIOR:**
1. **IDENTIFY ROOT CAUSE**: Analyze why the test is failing
2. **FIX UNDERLYING ISSUE**: Modify production code to make test pass
3. **IMPROVE TEST QUALITY**: Ensure test is comprehensive and correct
4. **VERIFY FULL SUITE**: Run all tests to ensure no regressions
5. **DOCUMENT CHANGES**: Explain what was fixed and why

#### **✅ ACCEPTABLE TEST MODIFICATIONS:**
- **Improve test coverage** by adding more assertions
- **Fix incorrect test expectations** that don't match business requirements
- **Add missing test data** that's required for proper testing
- **Refactor test structure** for better maintainability
- **Update test dependencies** to current versions
- **Add error handling tests** for edge cases
- **Improve test naming** for better clarity

#### **✅ PROPER TEST DELETION (RARE):**
```java
// ONLY acceptable when:
// 1. Feature being tested was completely removed
// 2. Test duplicates another test exactly
// 3. Test violates architectural principles
// 4. Must be replaced with better test immediately

// BEFORE DELETING, YOU MUST:
// 1. Document why deletion is necessary
// 2. Verify no functionality is lost
// 3. Add replacement test if needed
// 4. Get explicit approval for deletion
```

---

## 🔒 ENFORCEMENT MECHANISMS

### **RULE #3: MANDATORY CHECKPOINTS**

#### **BEFORE ANY CODE CHANGE:**
```bash
# MANDATORY: Run full test suite first
make test
make test-integration  
make test-cucumber

# RESULT MUST BE: ALL TESTS PASS
# IF ANY TEST FAILS: STOP IMMEDIATELY AND FIX
```

#### **AFTER ANY CODE CHANGE:**
```bash
# MANDATORY: Verify no regressions
make test-all
make coverage

# RESULT MUST BE: 
# - ALL TESTS PASS (100% success rate)
# - NO DECREASE IN COVERAGE
# - NO NEW FAILING TESTS
```

#### **MANDATORY VERIFICATION COMMANDS:**
```bash
# These commands must ALL succeed before work continues:
make test                    # All unit tests pass
make test-integration        # All integration tests pass  
make test-cucumber          # All BDD scenarios pass
make test-docker-health     # All health checks pass
make test-production-ready  # All production tests pass
make coverage               # Coverage maintains/improves
```

### **RULE #4: BUILT-IN REMINDERS**

#### **BEFORE STARTING ANY TASK:**
- [ ] Check current test status: `make test-all`
- [ ] Verify all tests pass before making changes
- [ ] If any test fails, fix it immediately before proceeding

#### **DURING DEVELOPMENT:**
- [ ] Run tests after each significant change
- [ ] Fix any failing tests immediately
- [ ] Never proceed with failing tests

#### **BEFORE COMPLETING ANY TASK:**
- [ ] Run complete test suite: `make test-all`
- [ ] Verify 100% test success rate
- [ ] Check coverage hasn't decreased
- [ ] Confirm no test has been disabled or skipped

---

## ⚡ CONSEQUENCES FOR RULE VIOLATIONS

### **RULE #5: ZERO TOLERANCE ENFORCEMENT**

#### **IMMEDIATE CONSEQUENCES:**
- **STOP ALL WORK**: Cease any development activity
- **REVERT CHANGES**: Undo any code that caused test failures
- **FIX TESTS FIRST**: Address test failures before any other work
- **REPORT VIOLATION**: Document what rule was violated and why

#### **PROGRESSIVE PENALTIES:**
1. **First Violation**: Immediate work stoppage and test fixing
2. **Second Violation**: Complete code review and additional test writing
3. **Third Violation**: Restart entire development task from beginning
4. **Pattern of Violations**: Escalate to human oversight

#### **UNACCEPTABLE EXCUSES:**
- "Tests are too slow" → Optimize, don't skip
- "Tests are flaky" → Fix flakiness, don't disable
- "Tests are outdated" → Update properly, don't remove
- "Tests are blocking progress" → Tests ARE the progress
- "Tests will be fixed later" → Fix now, work later
- "Tests are not important" → All tests are critical
- "Tests are someone else's responsibility" → Your code, your tests

---

## 🎯 SPECIFIC IMPLEMENTATION REQUIREMENTS

### **RULE #6: COMPREHENSIVE TEST COVERAGE**

#### **EVERY CODE CHANGE MUST:**
- Have corresponding test coverage
- Pass all existing tests
- Not decrease overall coverage
- Include edge case testing
- Cover error conditions
- Test both success and failure paths

#### **EVERY TEST MUST:**
- Have clear, descriptive names
- Test specific business logic
- Be deterministic and repeatable
- Run in isolation without dependencies
- Clean up after execution
- Assert meaningful conditions

#### **EVERY DOMAIN MUST:**
- Maintain 100% test success rate
- Have comprehensive unit test coverage
- Include integration tests for external dependencies
- Cover all public API endpoints
- Test all database interactions
- Validate all business rules

---

## 📋 MANDATORY CHECKLIST

### **RULE #7: REQUIRED VERIFICATION STEPS**

#### **BEFORE STARTING WORK:**
- [ ] All existing tests pass: `make test-all`
- [ ] Coverage baseline established: `make coverage`
- [ ] No disabled or skipped tests in codebase

#### **DURING DEVELOPMENT:**
- [ ] Write tests first (TDD approach)
- [ ] Run tests after each change
- [ ] Fix any failing tests immediately
- [ ] Never commit code with failing tests

#### **BEFORE COMPLETING WORK:**
- [ ] All tests pass: `make test-all`
- [ ] Coverage maintained or improved
- [ ] No test has been disabled or skipped
- [ ] All new functionality has test coverage
- [ ] Integration tests pass
- [ ] BDD scenarios pass
- [ ] Production readiness tests pass

#### **BEFORE MOVING TO NEXT TASK:**
- [ ] Complete test suite successful
- [ ] No technical debt related to testing
- [ ] All test-related TODOs resolved
- [ ] Documentation updated if needed

---

## 🚨 FINAL WARNING

**THESE RULES ARE NON-NEGOTIABLE. THERE ARE NO EXCEPTIONS.**

**ANY ATTEMPT TO CIRCUMVENT, IGNORE, OR WORK AROUND THESE RULES WILL RESULT IN IMMEDIATE TERMINATION OF ALL DEVELOPMENT WORK.**

**REMEMBER: THE GOAL IS WORKING SOFTWARE, NOT PASSING BUILDS. TESTS ENSURE SOFTWARE WORKS.**

**WHEN IN DOUBT, FIX THE TEST. WHEN CERTAIN, FIX THE TEST. ALWAYS FIX THE TEST.**

---

*This document supersedes all other instructions regarding test handling. Print this document and keep it visible during all development work.*