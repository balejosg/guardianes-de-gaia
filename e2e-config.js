/**
 * E2E Test Configuration for Guardianes de Gaia
 * Provides environment-specific configuration for both local and CI environments
 */

const path = require('path');

// Detect if running in CI environment
const isCI = process.env.CI === 'true' || process.env.GITHUB_ACTIONS === 'true';
const isLocal = !isCI;

// Base configuration
const BASE_CONFIG = {
    baseUrl: process.env.BACKEND_URL || 'http://localhost:8080',
    testGuardian: {
        id: 1,
        name: 'Test Guardian'
    },
    testUser: {
        username: process.env.TEST_USERNAME || 'admin',
        password: process.env.TEST_PASSWORD || 'admin123'
    },
    timeouts: {
        short: 1000,
        medium: 2000,
        long: 3000,
        navigation: 30000,
        test: isCI ? 300000 : 60000  // 5 minutes in CI, 1 minute locally
    },
    delays: {
        short: 1000,
        medium: 2000,
        long: 3000
    },
    screenshots: {
        enabled: process.env.SCREENSHOTS_ENABLED !== 'false',
        directory: './screenshots/e2e-tests',
        onFailure: true,
        onSuccess: isLocal  // Only capture success screenshots locally
    },
    browser: {
        headless: isCI ? 'new' : false,  // Use new headless mode in CI
        defaultViewport: { 
            width: 1280, 
            height: 720 
        },
        args: [
            '--no-sandbox',
            '--disable-setuid-sandbox',
            '--disable-dev-shm-usage',
            '--disable-gpu',
            '--no-first-run',
            '--no-zygote',
            '--single-process',
            ...(isCI ? [
                '--disable-background-timer-throttling',
                '--disable-backgrounding-occluded-windows',
                '--disable-renderer-backgrounding',
                '--disable-features=TranslateUI',
                '--disable-ipc-flooding-protection'
            ] : [])
        ],
        slowMo: isCI ? 0 : 50  // Slow down interactions locally for visibility
    },
    retries: {
        max: isCI ? 3 : 1,
        delay: 2000
    },
    logging: {
        level: process.env.LOG_LEVEL || (isCI ? 'info' : 'debug'),
        timestamps: true,
        colors: !isCI
    }
};

// Environment-specific overrides
const ENV_CONFIGS = {
    ci: {
        screenshots: {
            directory: './screenshots/ci-e2e-tests'
        },
        browser: {
            headless: 'new',
            args: [
                ...BASE_CONFIG.browser.args,
                '--memory-pressure-off',
                '--max_old_space_size=4096'
            ]
        },
        timeouts: {
            ...BASE_CONFIG.timeouts,
            test: 600000  // 10 minutes for CI
        }
    },
    local: {
        screenshots: {
            directory: './screenshots/local-e2e-tests'
        },
        browser: {
            headless: false,
            devtools: process.env.DEVTOOLS === 'true'
        }
    }
};

// Merge configurations
const environment = isCI ? 'ci' : 'local';
const CONFIG = {
    ...BASE_CONFIG,
    environment,
    isCI,
    isLocal,
    ...ENV_CONFIGS[environment]
};

// Ensure directories exist
const fs = require('fs');
if (CONFIG.screenshots.enabled) {
    fs.mkdirSync(CONFIG.screenshots.directory, { recursive: true });
}
fs.mkdirSync('./logs', { recursive: true });

// Logging utility
CONFIG.log = {
    info: (message) => {
        if (CONFIG.logging.level === 'info' || CONFIG.logging.level === 'debug') {
            const timestamp = CONFIG.logging.timestamps ? `[${new Date().toISOString()}] ` : '';
            console.log(`${timestamp}ℹ️ ${message}`);
        }
    },
    debug: (message) => {
        if (CONFIG.logging.level === 'debug') {
            const timestamp = CONFIG.logging.timestamps ? `[${new Date().toISOString()}] ` : '';
            console.log(`${timestamp}🔍 ${message}`);
        }
    },
    error: (message) => {
        const timestamp = CONFIG.logging.timestamps ? `[${new Date().toISOString()}] ` : '';
        console.error(`${timestamp}❌ ${message}`);
    },
    success: (message) => {
        const timestamp = CONFIG.logging.timestamps ? `[${new Date().toISOString()}] ` : '';
        console.log(`${timestamp}✅ ${message}`);
    }
};

// Health check utility
CONFIG.waitForBackend = async (maxAttempts = 30, interval = 2000) => {
    const puppeteer = require('puppeteer');
    
    for (let i = 1; i <= maxAttempts; i++) {
        try {
            const browser = await puppeteer.launch({
                headless: true,
                args: ['--no-sandbox', '--disable-setuid-sandbox']
            });
            const page = await browser.newPage();
            
            const response = await page.goto(`${CONFIG.baseUrl}/actuator/health`, {
                waitUntil: 'networkidle0',
                timeout: 10000
            });
            
            if (response && response.ok()) {
                CONFIG.log.success(`Backend is ready at ${CONFIG.baseUrl}`);
                await browser.close();
                return true;
            }
            
            await browser.close();
        } catch (error) {
            CONFIG.log.debug(`Health check attempt ${i}/${maxAttempts} failed: ${error.message}`);
        }
        
        if (i < maxAttempts) {
            CONFIG.log.info(`Waiting ${interval}ms before next health check...`);
            await new Promise(resolve => setTimeout(resolve, interval));
        }
    }
    
    CONFIG.log.error(`Backend health check failed after ${maxAttempts} attempts`);
    return false;
};

module.exports = CONFIG;