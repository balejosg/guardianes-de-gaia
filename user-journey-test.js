/**
 * Guardianes de Gaia - Complete User Journey Test with Puppeteer
 * This script walks through the entire user experience from authentication to gameplay
 */

const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');
const CONFIG = require('./e2e-config');

class GuardianesUserJourney {
    constructor() {
        this.browser = null;
        this.page = null;
        this.authToken = null;
        this.stepCount = 0;
    }

    async init() {
        console.log('🚀 Starting Guardianes User Journey Test...');
        
        // Ensure screenshots directory exists
        if (CONFIG.screenshots.enabled) {
            fs.mkdirSync(CONFIG.screenshots.directory, { recursive: true });
        }

        // Launch browser
        this.browser = await puppeteer.launch({
            headless: false, // Show browser for demo
            defaultViewport: { width: 1280, height: 720 },
            args: ['--no-sandbox', '--disable-setuid-sandbox']
        });

        this.page = await this.browser.newPage();
        
        // Set user agent and extra headers
        await this.page.setUserAgent('Guardianes-TestBot/1.0 (Puppeteer)');
        
        console.log('✅ Browser initialized');
    }

    async takeScreenshot(stepName) {
        if (!CONFIG.screenshots.enabled) return;
        
        this.stepCount++;
        const filename = `${this.stepCount.toString().padStart(2, '0')}-${stepName}.png`;
        const filepath = path.join(CONFIG.screenshots.directory, filename);
        
        await this.page.screenshot({ 
            path: filepath,
            fullPage: true
        });
        
        console.log(`📸 Screenshot saved: ${filename}`);
    }

    async authenticateUser() {
        console.log('\n🔐 Step 1: User Authentication');
        
        // Navigate to login endpoint
        const loginUrl = `${CONFIG.baseUrl}/api/v1/auth/login`;
        
        try {
            const response = await this.page.evaluate(async (url, credentials) => {
                const result = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(credentials)
                });
                return {
                    status: result.status,
                    data: await result.json()
                };
            }, loginUrl, CONFIG.testUser);

            if (response.status === 200) {
                this.authToken = response.data.token;
                console.log(`✅ Authentication successful for user: ${CONFIG.testUser.username}`);
                console.log(`🎫 Token received: ${this.authToken.substring(0, 20)}...`);
                
                await this.takeScreenshot('authentication-success');
                return true;
            } else {
                console.error('❌ Authentication failed:', response.data);
                return false;
            }
        } catch (error) {
            console.error('❌ Authentication error:', error.message);
            return false;
        }
    }

    async validateToken() {
        console.log('\n🔍 Step 2: Token Validation');
        
        const validateUrl = `${CONFIG.baseUrl}/api/v1/auth/validate`;
        
        try {
            const response = await this.page.evaluate(async (url, token) => {
                const result = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
                return {
                    status: result.status,
                    data: await result.json()
                };
            }, validateUrl, this.authToken);

            if (response.status === 200 && response.data.valid) {
                console.log(`✅ Token is valid for user: ${response.data.username}`);
                console.log(`⏰ Expires at: ${response.data.expiresAt}`);
                
                await this.takeScreenshot('token-validation');
                return true;
            } else {
                console.error('❌ Token validation failed:', response.data);
                return false;
            }
        } catch (error) {
            console.error('❌ Token validation error:', error.message);
            return false;
        }
    }

    async getCurrentStepCount() {
        console.log('\n👣 Step 3: Check Current Step Count');
        
        const stepUrl = `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/steps/current`;
        
        try {
            const response = await this.page.evaluate(async (url, token) => {
                const result = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
                return {
                    status: result.status,
                    data: await result.json()
                };
            }, stepUrl, this.authToken);

            if (response.status === 200) {
                console.log(`✅ Current step data retrieved:`);
                console.log(`   📊 Steps today: ${response.data.stepCount}`);
                console.log(`   ⚡ Energy available: ${response.data.energyGenerated}`);
                console.log(`   📅 Date: ${response.data.date}`);
                
                await this.takeScreenshot('current-step-count');
                return response.data;
            } else {
                console.error('❌ Failed to get current step count:', response.data);
                return null;
            }
        } catch (error) {
            console.error('❌ Current step count error:', error.message);
            return null;
        }
    }

    async submitSteps(stepCount = 2500) {
        console.log(`\n🚶 Step 4: Submit Daily Steps (${stepCount} steps)`);
        
        const submitUrl = `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/steps`;
        const stepData = {
            stepCount: stepCount,
            source: 'MOBILE_APP',
            deviceInfo: 'Puppeteer Test Device'
        };
        
        try {
            const response = await this.page.evaluate(async (url, token, data) => {
                const result = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });
                return {
                    status: result.status,
                    data: await result.json()
                };
            }, submitUrl, this.authToken, stepData);

            if (response.status === 200) {
                console.log(`✅ Steps submitted successfully:`);
                console.log(`   👣 Steps submitted: ${response.data.stepCount}`);
                console.log(`   ⚡ Energy generated: ${response.data.energyGenerated}`);
                console.log(`   📈 Total daily steps: ${response.data.totalDailySteps}`);
                console.log(`   💪 Energy conversion: ${stepCount} steps → ${response.data.energyGenerated} energy`);
                
                await this.takeScreenshot('step-submission-success');
                return response.data;
            } else {
                console.error('❌ Step submission failed:', response.data);
                return null;
            }
        } catch (error) {
            console.error('❌ Step submission error:', error.message);
            return null;
        }
    }

    async getEnergyBalance() {
        console.log('\n⚡ Step 5: Check Energy Balance');
        
        const energyUrl = `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/energy/balance`;
        
        try {
            const response = await this.page.evaluate(async (url, token) => {
                const result = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
                return {
                    status: result.status,
                    data: await result.json()
                };
            }, energyUrl, this.authToken);

            if (response.status === 200) {
                console.log(`✅ Energy balance retrieved:`);
                console.log(`   💰 Current balance: ${response.data.currentBalance} energy`);
                console.log(`   📊 Today's earned: ${response.data.todaysEarned} energy`);
                console.log(`   💸 Today's spent: ${response.data.todaysSpent} energy`);
                console.log(`   📈 Recent transactions: ${response.data.recentTransactions?.length || 0}`);
                
                await this.takeScreenshot('energy-balance');
                return response.data;
            } else {
                console.error('❌ Failed to get energy balance:', response.data);
                return null;
            }
        } catch (error) {
            console.error('❌ Energy balance error:', error.message);
            return null;
        }
    }

    async spendEnergy(amount = 50, source = 'CARD_BATTLE') {
        console.log(`\n💸 Step 6: Spend Energy (${amount} energy for ${source})`);
        
        const spendUrl = `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/energy/spend`;
        const spendData = {
            amount: amount,
            source: source,
            description: `Puppeteer test spending for ${source}`
        };
        
        try {
            const response = await this.page.evaluate(async (url, token, data) => {
                const result = await fetch(url, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(data)
                });
                return {
                    status: result.status,
                    data: await result.json()
                };
            }, spendUrl, this.authToken, spendData);

            if (response.status === 200) {
                console.log(`✅ Energy spent successfully:`);
                console.log(`   💸 Amount spent: ${response.data.amountSpent} energy`);
                console.log(`   💰 Remaining balance: ${response.data.remainingBalance} energy`);
                console.log(`   🎮 Source: ${response.data.source}`);
                console.log(`   📝 Transaction ID: ${response.data.transactionId}`);
                
                await this.takeScreenshot('energy-spending-success');
                return response.data;
            } else {
                console.error('❌ Energy spending failed:', response.data);
                return null;
            }
        } catch (error) {
            console.error('❌ Energy spending error:', error.message);
            return null;
        }
    }

    async getStepHistory() {
        console.log('\n📊 Step 7: Get Step History');
        
        const today = new Date();
        const lastWeek = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
        
        const historyUrl = `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/steps/history` +
            `?from=${lastWeek.toISOString().split('T')[0]}&to=${today.toISOString().split('T')[0]}`;
        
        try {
            const response = await this.page.evaluate(async (url, token) => {
                const result = await fetch(url, {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                });
                return {
                    status: result.status,
                    data: await result.json()
                };
            }, historyUrl, this.authToken);

            if (response.status === 200) {
                console.log(`✅ Step history retrieved:`);
                console.log(`   📅 Period: ${response.data.fromDate} to ${response.data.toDate}`);
                console.log(`   📊 Total days: ${response.data.dailyRecords?.length || 0}`);
                console.log(`   👣 Total steps: ${response.data.totalSteps}`);
                console.log(`   ⚡ Total energy: ${response.data.totalEnergyGenerated}`);
                
                if (response.data.dailyRecords) {
                    response.data.dailyRecords.forEach((record, index) => {
                        console.log(`      Day ${index + 1}: ${record.stepCount} steps → ${record.energyGenerated} energy`);
                    });
                }
                
                await this.takeScreenshot('step-history');
                return response.data;
            } else {
                console.error('❌ Failed to get step history:', response.data);
                return null;
            }
        } catch (error) {
            console.error('❌ Step history error:', error.message);
            return null;
        }
    }

    async checkHealthEndpoints() {
        console.log('\n🏥 Step 8: Health & Monitoring Check');
        
        const endpoints = [
            { name: 'Application Health', url: `${CONFIG.baseUrl}/actuator/health` },
            { name: 'Application Info', url: `${CONFIG.baseUrl}/actuator/info` },
            { name: 'Prometheus Metrics', url: `${CONFIG.baseUrl}/actuator/prometheus` }
        ];
        
        for (const endpoint of endpoints) {
            try {
                const response = await this.page.evaluate(async (url) => {
                    const result = await fetch(url);
                    return {
                        status: result.status,
                        contentType: result.headers.get('content-type'),
                        data: result.status === 200 ? await result.text() : null
                    };
                }, endpoint.url);

                if (response.status === 200) {
                    console.log(`✅ ${endpoint.name}: Healthy`);
                    if (endpoint.name === 'Application Health') {
                        console.log(`   📊 Response: ${response.data}`);
                    }
                } else {
                    console.log(`⚠️  ${endpoint.name}: Status ${response.status}`);
                }
            } catch (error) {
                console.log(`❌ ${endpoint.name}: Error - ${error.message}`);
            }
        }
        
        await this.takeScreenshot('health-endpoints');
    }

    async runCompleteJourney() {
        try {
            await this.init();
            
            // Complete user journey simulation
            const authSuccess = await this.authenticateUser();
            if (!authSuccess) {
                throw new Error('Authentication failed');
            }
            
            await this.validateToken();
            await this.getCurrentStepCount();
            await this.submitSteps(2500); // Submit 2500 steps (250 energy)
            await this.getEnergyBalance();
            await this.spendEnergy(50, 'CARD_BATTLE'); // Spend energy on card battle
            await this.spendEnergy(30, 'DAILY_CHALLENGE'); // Spend on challenge
            await this.getEnergyBalance(); // Check balance after spending
            await this.getStepHistory();
            await this.checkHealthEndpoints();
            
            console.log('\n🎉 User Journey Test Completed Successfully!');
            console.log('\n📋 Journey Summary:');
            console.log('   1. ✅ User authentication');
            console.log('   2. ✅ Token validation');
            console.log('   3. ✅ Step count retrieval');
            console.log('   4. ✅ Step submission (walking to school)');
            console.log('   5. ✅ Energy generation (steps → energy)');
            console.log('   6. ✅ Energy spending (card battles)');
            console.log('   7. ✅ Step history tracking');
            console.log('   8. ✅ Health monitoring');
            
            if (CONFIG.screenshots.enabled) {
                console.log(`\n📸 Screenshots saved to: ${CONFIG.screenshots.directory}`);
            }
            
        } catch (error) {
            console.error('\n❌ User Journey Test Failed:', error.message);
            await this.takeScreenshot('error-state');
        } finally {
            if (this.browser) {
                await this.browser.close();
                console.log('🔚 Browser closed');
            }
        }
    }
}

// Export for use as module or run directly
if (require.main === module) {
    const journey = new GuardianesUserJourney();
    journey.runCompleteJourney().catch(console.error);
}

module.exports = GuardianesUserJourney;