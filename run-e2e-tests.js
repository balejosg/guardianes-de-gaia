#!/usr/bin/env node

/**
 * E2E Test Runner for Guardianes de Gaia
 * Orchestrates the execution of all end-to-end tests with proper CI/CD integration
 */

const CONFIG = require('./e2e-config');
const { execSync, spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

class E2ETestRunner {
    constructor() {
        this.testResults = [];
        this.startTime = Date.now();
    }

    async runTest(testFile, description) {
        CONFIG.log.info(`\n🧪 Running ${description}...`);
        const testStartTime = Date.now();
        
        return new Promise((resolve) => {
            const testProcess = spawn('node', [testFile], {
                stdio: ['inherit', 'pipe', 'pipe'],
                env: { ...process.env, CI: process.env.CI || 'false' }
            });

            let stdout = '';
            let stderr = '';

            testProcess.stdout.on('data', (data) => {
                stdout += data.toString();
                if (CONFIG.logging.level === 'debug') {
                    process.stdout.write(data);
                }
            });

            testProcess.stderr.on('data', (data) => {
                stderr += data.toString();
                if (CONFIG.logging.level === 'debug') {
                    process.stderr.write(data);
                }
            });

            testProcess.on('close', (code) => {
                const duration = Date.now() - testStartTime;
                const result = {
                    testFile,
                    description,
                    success: code === 0,
                    duration,
                    stdout,
                    stderr,
                    exitCode: code
                };

                this.testResults.push(result);

                if (code === 0) {
                    CONFIG.log.success(`✅ ${description} passed (${duration}ms)`);
                } else {
                    CONFIG.log.error(`❌ ${description} failed with exit code ${code} (${duration}ms)`);
                    if (stderr) {
                        CONFIG.log.error(`Error output: ${stderr.slice(0, 500)}...`);
                    }
                }

                resolve(result);
            });

            testProcess.on('error', (error) => {
                CONFIG.log.error(`Failed to start test ${testFile}: ${error.message}`);
                resolve({
                    testFile,
                    description,
                    success: false,
                    duration: Date.now() - testStartTime,
                    error: error.message,
                    exitCode: -1
                });
            });
        });
    }

    async runAllTests() {
        CONFIG.log.info('🚀 Starting Guardianes de Gaia E2E Test Suite');
        CONFIG.log.info(`Environment: ${CONFIG.environment}`);
        CONFIG.log.info(`Backend URL: ${CONFIG.baseUrl}`);

        // Ensure backend is ready before starting tests
        CONFIG.log.info('🔍 Checking backend availability...');
        const backendReady = await CONFIG.waitForBackend(15, 5000); // 15 attempts, 5 seconds apart
        
        if (!backendReady) {
            CONFIG.log.error('❌ Backend is not available. Cannot run E2E tests.');
            process.exit(1);
        }

        // Define test suite
        const tests = [
            {
                file: 'functional-api-test.js',
                description: 'Functional API Tests'
            },
            {
                file: 'simple-journey-test.js',
                description: 'Simple User Journey Tests'
            },
            {
                file: 'user-journey-test.js',
                description: 'Complete User Journey Tests'
            },
            {
                file: 'visual-journey-test.js',
                description: 'Visual Interface Tests'
            }
        ];

        // Filter tests based on command line arguments
        const requestedTest = process.argv[2];
        const filteredTests = requestedTest 
            ? tests.filter(test => test.file.includes(requestedTest))
            : tests;

        if (filteredTests.length === 0) {
            CONFIG.log.error(`❌ No tests found matching: ${requestedTest}`);
            process.exit(1);
        }

        CONFIG.log.info(`📋 Running ${filteredTests.length} test suite(s):`);
        filteredTests.forEach(test => CONFIG.log.info(`  - ${test.description}`));

        // Run tests sequentially to avoid resource conflicts
        for (const test of filteredTests) {
            if (!fs.existsSync(test.file)) {
                CONFIG.log.error(`❌ Test file not found: ${test.file}`);
                this.testResults.push({
                    testFile: test.file,
                    description: test.description,
                    success: false,
                    duration: 0,
                    error: 'Test file not found',
                    exitCode: -1
                });
                continue;
            }

            // Add delay between tests to allow cleanup
            if (this.testResults.length > 0) {
                CONFIG.log.info('⏳ Waiting between tests...');
                await new Promise(resolve => setTimeout(resolve, 5000));
            }

            await this.runTest(test.file, test.description);
        }

        this.generateReport();
        this.exitWithResults();
    }

    generateReport() {
        const totalDuration = Date.now() - this.startTime;
        const successfulTests = this.testResults.filter(r => r.success).length;
        const failedTests = this.testResults.length - successfulTests;

        CONFIG.log.info('\n📊 E2E Test Suite Results');
        CONFIG.log.info('═'.repeat(50));
        CONFIG.log.info(`Total tests: ${this.testResults.length}`);
        CONFIG.log.info(`Successful: ${successfulTests}`);
        CONFIG.log.info(`Failed: ${failedTests}`);
        CONFIG.log.info(`Total duration: ${totalDuration}ms`);
        CONFIG.log.info('═'.repeat(50));

        // Detailed results
        this.testResults.forEach((result, index) => {
            const status = result.success ? '✅' : '❌';
            CONFIG.log.info(`${status} ${result.description} (${result.duration}ms)`);
            
            if (!result.success) {
                if (result.error) {
                    CONFIG.log.error(`   Error: ${result.error}`);
                }
                if (result.exitCode !== undefined) {
                    CONFIG.log.error(`   Exit code: ${result.exitCode}`);
                }
            }
        });

        // Generate JSON report for CI
        const reportPath = './e2e-test-results.json';
        const report = {
            timestamp: new Date().toISOString(),
            environment: CONFIG.environment,
            backend_url: CONFIG.baseUrl,
            total_tests: this.testResults.length,
            successful_tests: successfulTests,
            failed_tests: failedTests,
            total_duration_ms: totalDuration,
            success_rate: (successfulTests / this.testResults.length * 100).toFixed(2),
            results: this.testResults
        };

        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        CONFIG.log.info(`📄 Test report saved to: ${reportPath}`);

        return failedTests === 0;
    }

    exitWithResults() {
        const allTestsPassed = this.testResults.every(r => r.success);
        
        if (allTestsPassed) {
            CONFIG.log.success('\n🎉 All E2E tests passed successfully!');
            process.exit(0);
        } else {
            CONFIG.log.error('\n💥 Some E2E tests failed. Check the results above.');
            process.exit(1);
        }
    }
}

// Script execution
if (require.main === module) {
    const runner = new E2ETestRunner();
    
    // Handle process termination
    process.on('SIGINT', () => {
        CONFIG.log.info('\n🛑 Test execution interrupted');
        process.exit(1);
    });

    process.on('unhandledRejection', (error) => {
        CONFIG.log.error(`Unhandled rejection: ${error.message}`);
        process.exit(1);
    });

    // Run tests
    runner.runAllTests().catch((error) => {
        CONFIG.log.error(`Fatal error in test runner: ${error.message}`);
        process.exit(1);
    });
}

module.exports = E2ETestRunner;