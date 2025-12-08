# CI/CD Rules

## Critical Rules

### Test Failures
**The CI workflow will NEVER continue on test failures.** All tests must pass for CI to succeed.

- If tests fail, CI fails - no exceptions
- Do NOT add `continue-on-error: true` to test steps
- Fix the tests, don't bypass them

### Coverage Requirements
- 70% code coverage threshold must be met
- Coverage check runs AFTER tests pass
