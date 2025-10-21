#!/bin/bash

echo "=== FutureX Course Catalog Verification Script ==="
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Function to check if service is running
check_service() {
    local url=$1
    local service_name=$2
    
    if curl -s "$url" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ $service_name is running${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name is not running${NC}"
        return 1
    fi
}

# Function to test endpoint
test_endpoint() {
    local url=$1
    local endpoint_name=$2
    
    echo -n "Testing $endpoint_name... "
    response=$(curl -s -w "%{http_code}" "$url")
    http_code="${response: -3}"
    
    if [ "$http_code" = "200" ]; then
        echo -e "${GREEN}✅ OK${NC}"
        echo "Response: ${response%???}"
        echo
    else
        echo -e "${RED}❌ Failed (HTTP $http_code)${NC}"
        echo
    fi
}

echo "1. Checking prerequisites..."
check_service "http://localhost:8761/" "Eureka Server"
echo

echo "2. Checking if FutureX Course Catalog is running..."
check_service "http://localhost:8002/actuator/health" "Course Catalog Service"
echo

echo "3. Testing endpoints with course service available..."
check_service "http://localhost:8080/" "Course Service"

test_endpoint "http://localhost:8002/" "Home endpoint"
test_endpoint "http://localhost:8002/catalog" "Catalog endpoint"
test_endpoint "http://localhost:8002/firstcourse" "First course endpoint"

echo "4. Testing Actuator endpoints..."
test_endpoint "http://localhost:8002/actuator/health" "Health endpoint"
test_endpoint "http://localhost:8002/actuator/circuitbreakers" "Circuit Breakers"

echo "5. Testing Circuit Breaker fallbacks..."
echo -e "${YELLOW}Note: Stop the fx-course-service and test again to verify fallbacks${NC}"
echo

echo "=== Verification Complete ==="