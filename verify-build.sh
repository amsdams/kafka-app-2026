#!/bin/bash

# Build Verification Script
# This script verifies the project structure and dependencies

set -e

echo "================================================"
echo "Build Verification Script"
echo "================================================"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check Java
echo -n "Checking Java... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo -e "${GREEN}✓ Found Java $JAVA_VERSION${NC}"
else
    echo -e "${RED}✗ Java not found. Please install Java 25+${NC}"
    exit 1
fi

# Check Maven
echo -n "Checking Maven... "
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    echo -e "${GREEN}✓ Found Maven $MVN_VERSION${NC}"
else
    echo -e "${RED}✗ Maven not found. Please install Maven 3.6+${NC}"
    exit 1
fi

# Check Docker
echo -n "Checking Docker... "
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | cut -d' ' -f3 | tr -d ',')
    echo -e "${GREEN}✓ Found Docker $DOCKER_VERSION${NC}"
else
    echo -e "${YELLOW}⚠ Docker not found (optional for build)${NC}"
fi

# Check Docker Compose
echo -n "Checking Docker Compose... "
if command -v docker-compose &> /dev/null; then
    COMPOSE_VERSION=$(docker-compose --version | cut -d' ' -f4 | tr -d ',')
    echo -e "${GREEN}✓ Found Docker Compose $COMPOSE_VERSION${NC}"
else
    echo -e "${YELLOW}⚠ Docker Compose not found (optional for build)${NC}"
fi

echo ""
echo "================================================"
echo "Verifying Project Structure"
echo "================================================"
echo ""

# Check for required files
FILES=(
    "pom.xml"
    "shared-models/pom.xml"
    "producer-service/pom.xml"
    "consumer-service/pom.xml"
    "docker-compose.yml"
)

for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file"
    else
        echo -e "${RED}✗${NC} $file (MISSING)"
        exit 1
    fi
done

echo ""
echo "================================================"
echo "Building Project"
echo "================================================"
echo ""

# Clean
echo "→ Cleaning previous build..."
mvn clean > /dev/null 2>&1 || true

# Build shared-models
echo "→ Building shared-models..."
if mvn -f shared-models/pom.xml install -DskipTests; then
    echo -e "${GREEN}✓${NC} shared-models built successfully"
else
    echo -e "${RED}✗${NC} shared-models build failed"
    exit 1
fi

# Build producer-service
echo "→ Building producer-service..."
if mvn -f producer-service/pom.xml package -DskipTests; then
    echo -e "${GREEN}✓${NC} producer-service built successfully"
else
    echo -e "${RED}✗${NC} producer-service build failed"
    exit 1
fi

# Build consumer-service
echo "→ Building consumer-service..."
if mvn -f consumer-service/pom.xml package -DskipTests; then
    echo -e "${GREEN}✓${NC} consumer-service built successfully"
else
    echo -e "${RED}✗${NC} consumer-service build failed"
    exit 1
fi

echo ""
echo "================================================"
echo -e "${GREEN}✓ Build Verification Successful!${NC}"
echo "================================================"
echo ""
echo "You can now:"
echo "  1. Run with Docker: ./build-and-run.sh"
echo "  2. Run with Make:   make up"
echo "  3. Manual Docker:   docker-compose up --build"
echo ""
