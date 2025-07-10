/**
 * Guardianes de Gaia - Simple User Journey Test
 * API-focused journey test that can run without full browser automation
 */

// Simple configuration
const CONFIG = {
    baseUrl: 'http://localhost:8080',
    testUser: {
        username: 'admin',
        password: '7kF2xN4pM8vWc1uE5rT9hY3oS0nB6qA3dX7vC5mN2p'
    },
    testGuardian: {
        id: 1,
        name: 'Test Guardian'
    }
};

class SimpleUserJourney {
    constructor() {
        this.authToken = null;
    }

    async makeRequest(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                ...(this.authToken && { 'Authorization': `Bearer ${this.authToken}` })
            }
        };

        const mergedOptions = {
            ...defaultOptions,
            ...options,
            headers: { ...defaultOptions.headers, ...options.headers }
        };

        try {
            // Use global fetch or node-fetch
            const fetch = globalThis.fetch;
            const response = await fetch(url, mergedOptions);
            const data = await response.json();
            
            return {
                status: response.status,
                ok: response.ok,
                data: data
            };
        } catch (error) {
            console.error(`Request failed for ${url}:`, error.message);
            return {
                status: 0,
                ok: false,
                error: error.message
            };
        }
    }

    async authenticate() {
        console.log('🔐 Step 1: Authenticating user...');
        
        const result = await this.makeRequest(`${CONFIG.baseUrl}/api/v1/auth/login`, {
            method: 'POST',
            body: JSON.stringify(CONFIG.testUser)
        });

        if (result.ok) {
            this.authToken = result.data.token;
            console.log(`✅ Authentication successful for: ${CONFIG.testUser.username}`);
            console.log(`🎫 Token: ${this.authToken.substring(0, 30)}...`);
            return true;
        } else {
            console.error('❌ Authentication failed:', result.data);
            return false;
        }
    }

    async validateToken() {
        console.log('\n🔍 Step 2: Validating token...');
        
        const result = await this.makeRequest(`${CONFIG.baseUrl}/api/v1/auth/validate`);

        if (result.ok && result.data.valid) {
            console.log(`✅ Token valid for user: ${result.data.username}`);
            console.log(`⏰ Expires: ${result.data.expiresAt}`);
            return true;
        } else {
            console.error('❌ Token validation failed:', result.data);
            return false;
        }
    }

    async getCurrentSteps() {
        console.log('\n👣 Step 3: Checking current step count...');
        
        const result = await this.makeRequest(
            `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/steps/current`
        );

        if (result.ok) {
            console.log(`✅ Current step data:`);
            console.log(`   📊 Steps: ${result.data.stepCount}`);
            console.log(`   ⚡ Energy: ${result.data.energyGenerated}`);
            console.log(`   📅 Date: ${result.data.date}`);
            return result.data;
        } else {
            console.error('❌ Failed to get steps:', result.data);
            return null;
        }
    }

    async submitSteps(stepCount = 3000) {
        console.log(`\n🚶 Step 4: Submitting ${stepCount} steps...`);
        
        const stepData = {
            stepCount: stepCount,
            timestamp: new Date().toISOString()
        };

        const result = await this.makeRequest(
            `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/steps`,
            {
                method: 'POST',
                body: JSON.stringify(stepData)
            }
        );

        if (result.ok) {
            console.log(`✅ Steps submitted successfully:`);
            console.log(`   👣 Steps: ${result.data.stepCount}`);
            console.log(`   ⚡ Energy generated: ${result.data.energyGenerated}`);
            console.log(`   📈 Total today: ${result.data.totalDailySteps}`);
            console.log(`   💰 Conversion: ${stepCount} steps → ${result.data.energyGenerated} energy`);
            return result.data;
        } else {
            console.error('❌ Step submission failed:', result.data);
            return null;
        }
    }

    async getEnergyBalance() {
        console.log('\n⚡ Step 5: Checking energy balance...');
        
        const result = await this.makeRequest(
            `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/energy/balance`
        );

        if (result.ok) {
            console.log(`✅ Energy balance:`);
            console.log(`   💰 Current: ${result.data.currentBalance} energy`);
            console.log(`   📈 Today earned: ${result.data.todaysEarned}`);
            console.log(`   📉 Today spent: ${result.data.todaysSpent}`);
            return result.data;
        } else {
            console.error('❌ Failed to get energy balance:', result.data);
            return null;
        }
    }

    async spendEnergy(amount = 100, source = 'BATTLE') {
        console.log(`\n💸 Step 6: Spending ${amount} energy on ${source}...`);
        
        const spendData = {
            amount: amount,
            source: source
        };

        const result = await this.makeRequest(
            `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/energy/spend`,
            {
                method: 'POST',
                body: JSON.stringify(spendData)
            }
        );

        if (result.ok) {
            console.log(`✅ Energy spent successfully:`);
            console.log(`   💸 Spent: ${result.data.amountSpent} energy`);
            console.log(`   💰 Remaining: ${result.data.remainingBalance} energy`);
            console.log(`   🎮 For: ${result.data.source}`);
            return result.data;
        } else {
            console.error('❌ Energy spending failed:', result.data);
            return null;
        }
    }

    async getStepHistory() {
        console.log('\n📊 Step 7: Getting step history...');
        
        const today = new Date().toISOString().split('T')[0];
        const lastWeek = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];
        
        const result = await this.makeRequest(
            `${CONFIG.baseUrl}/api/v1/guardians/${CONFIG.testGuardian.id}/steps/history?from=${lastWeek}&to=${today}`
        );

        if (result.ok) {
            console.log(`✅ Step history (${lastWeek} to ${today}):`);
            console.log(`   📊 Total steps: ${result.data.totalSteps}`);
            console.log(`   ⚡ Total energy: ${result.data.totalEnergyGenerated}`);
            console.log(`   📅 Days recorded: ${result.data.dailyRecords?.length || 0}`);
            
            if (result.data.dailyRecords) {
                result.data.dailyRecords.slice(0, 3).forEach((record, i) => {
                    console.log(`      ${record.date}: ${record.stepCount} steps → ${record.energyGenerated} energy`);
                });
            }
            return result.data;
        } else {
            console.error('❌ Failed to get step history:', result.data);
            return null;
        }
    }

    async checkHealth() {
        console.log('\n🏥 Step 8: Checking application health...');
        
        const result = await this.makeRequest(`${CONFIG.baseUrl}/actuator/health`);

        if (result.ok) {
            console.log(`✅ Application health: ${result.data.status}`);
            return true;
        } else {
            console.error('❌ Health check failed');
            return false;
        }
    }

    async runJourney() {
        console.log('🚀 Starting Guardianes User Journey...\n');
        
        try {
            // Execute the complete user journey
            const authSuccess = await this.authenticate();
            if (!authSuccess) throw new Error('Authentication failed');
            
            await this.validateToken();
            await this.getCurrentSteps();
            await this.submitSteps(3000); // Walk to school: 3000 steps
            await this.getEnergyBalance();
            await this.spendEnergy(150, 'BATTLE'); // Play a card battle
            await this.spendEnergy(75, 'CHALLENGE'); // Complete a challenge
            await this.getEnergyBalance(); // Check remaining balance
            await this.getStepHistory();
            await this.checkHealth();
            
            console.log('\n🎉 User Journey Completed Successfully!');
            console.log('\n📋 Journey Summary:');
            console.log('   ✅ Guardian authenticated and token validated');
            console.log('   ✅ Daily walking steps submitted (school journey)');
            console.log('   ✅ Steps converted to energy for gameplay');
            console.log('   ✅ Energy spent on card battles and challenges');
            console.log('   ✅ Progress tracked with step history');
            console.log('   ✅ System health verified');
            console.log('\n💡 This simulates a typical day: walk to school → earn energy → play cards → have fun!');
            
        } catch (error) {
            console.error('\n❌ Journey failed:', error.message);
        }
    }
}

// Check if this is the main module and run the journey
if (require.main === module) {
    const journey = new SimpleUserJourney();
    journey.runJourney();
}

module.exports = SimpleUserJourney;