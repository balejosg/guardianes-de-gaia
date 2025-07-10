#!/bin/bash

# Quick Demo Check Script
# This script runs essential tests to validate demo readiness quickly

set -e

echo "🏃 Quick Demo Validation Check..."
echo "================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    if [ "$status" = "SUCCESS" ]; then
        echo -e "${GREEN}✅ $message${NC}"
    elif [ "$status" = "FAILURE" ]; then
        echo -e "${RED}❌ $message${NC}"
    elif [ "$status" = "INFO" ]; then
        echo -e "${YELLOW}ℹ️  $message${NC}"
    fi
}

# Check if we're in the right directory
if [ ! -f "backend/pom.xml" ]; then
    print_status "FAILURE" "Please run this script from the project root directory"
    exit 1
fi

# Quick backend API validation
print_status "INFO" "Validating backend API..."
cd backend
if mvn test -Dtest=DemoValidationIntegrationTest#shouldCompleteFullStepTrackingDemoFlow -q; then
    print_status "SUCCESS" "Backend API validation passed"
else
    print_status "FAILURE" "Backend API validation failed"
    exit 1
fi

# Quick Flutter model validation
print_status "INFO" "Validating Flutter models..."
cd ../mobile/guardianes_mobile
if ../../flutter/bin/flutter test test/features/step_tracking/data/models/step_models_test.dart --no-pub > /dev/null 2>&1; then
    print_status "SUCCESS" "Flutter model validation passed"
else
    print_status "FAILURE" "Flutter model validation failed"
    exit 1
fi

cd ../..

# Quick JSON serialization check
print_status "INFO" "Validating JSON serialization..."
cd backend
if mvn test -Dtest=JsonSerializationTest#shouldSerializeStepSubmissionResponse -q; then
    print_status "SUCCESS" "JSON serialization validation passed"
else
    print_status "FAILURE" "JSON serialization validation failed"
    exit 1
fi

cd ..

echo ""
print_status "SUCCESS" "🎉 Quick Demo Check Completed Successfully!"
echo "Demo is ready for presentation! 🚀"
echo ""
echo "Core validations passed:"
echo "- Step tracking demo flow: ✅"
echo "- Flutter JSON parsing: ✅"
echo "- Backend serialization: ✅"
echo ""
echo "For comprehensive validation, run: ./scripts/run_demo_validation.sh"