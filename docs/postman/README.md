# Postman API Testing Configuration

This directory contains Postman collections and environments for testing the Guardianes de Gaia API.

## 📦 Files

- `Guardianes-de-Gaia-API.postman_collection.json` - Complete API test collection
- `Guardianes-Local.postman_environment.json` - Local development environment variables

## 🚀 Quick Setup

### 1. Import Collection
1. Open Postman
2. Click **Import** button
3. Select `Guardianes-de-Gaia-API.postman_collection.json`
4. Click **Import**

### 2. Import Environment
1. Click **Import** button again
2. Select `Guardianes-Local.postman_environment.json`
3. Click **Import**

### 3. Set Environment
1. In the top-right corner, select **Guardianes - Local Development** from the environment dropdown
2. Ensure your Docker environment is running: `make up`

## ✅ Testing Workflow

### 🚀 **W1 Quick Test - Recommended for first-time testing**
For the fastest verification of W1: Basic Step Tracking functionality:

1. Run the **"⚡ W1 Quick Test - Basic Step Tracking"** folder in sequence:
   - **1. Health Check** - Verify API is running
   - **2. Submit 1000 Steps** - Submit steps and earn 100 energy  
   - **3. Check Current Steps & Energy** - Verify step tracking
   - **4. Check Energy Balance** - Verify energy transactions

### Advanced Testing Flow
1. **Get Current Step Count** - Check initial state
2. **Submit Steps - Valid** - Submit 2500 steps (earns 250 energy)
3. **Get Energy Balance** - Verify energy was earned
4. **Spend Energy - Valid** - Spend 100 energy on battle
5. **Get Energy Balance** - Verify balance updated

### Validation Testing
- **Submit Steps - Negative** - Tests validation (should fail with 400)
- **Submit Steps - Anomalous** - Tests fraud detection (should fail with 400)
- **Spend Energy - Insufficient** - Tests energy validation (should fail with 400)

## 🧪 Test Scenarios

The collection includes complete end-to-end scenarios:

- **⚡ W1 Quick Test**: Fast verification of basic step tracking (4 requests)
- **Scenario 1 - Basic Flow**: Complete happy path testing (5000 steps → earn energy → spend energy)
- Individual endpoint testing with validation
- Error handling verification

## 🔧 Environment Variables

| Variable | Value | Description |
|----------|--------|-------------|
| `base_url` | `http://localhost:8080` | API base URL |
| `auth_username` | `admin` | Basic auth username |
| `auth_password` | `dev123` | Basic auth password |
| `guardian_id` | `1` | Default Guardian ID |

## 📊 Automated Testing

The collection includes automated tests that:
- Validate response codes
- Check response structure
- Verify business logic (10 steps = 1 energy)
- Update environment variables dynamically
- Test error scenarios

## 🐛 Troubleshooting

### API Not Responding
```bash
# Check if services are running
make logs

# Restart if needed
make down && make up
```

### Authentication Errors
- Verify environment is set to "Guardianes - Local Development"
- Check auth credentials in environment variables

### Test Failures
- Run tests in sequence (don't run in parallel)
- Some tests depend on previous test results
- Use **Scenario 1 - Basic Flow** for complete workflow testing