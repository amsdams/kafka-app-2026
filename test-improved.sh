#!/bin/bash

echo "🧪 Testing Kafka Microservices - Improved Version"
echo "=================================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Wait for services to be ready
echo -e "${BLUE}⏳ Waiting for services to be ready...${NC}"
sleep 5

# Test 1: Create User Event
echo -e "\n${GREEN}📝 Test 1: Creating User Event${NC}"
USER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/events/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "eventType": "USER_CREATED"
  }')

echo "Response: $USER_RESPONSE"
USER_ID=$(echo $USER_RESPONSE | jq -r '.eventId')
echo -e "${YELLOW}User ID: $USER_ID${NC}"

sleep 2

# Test 2: Create Order Event
echo -e "\n${GREEN}📦 Test 2: Creating Order Event (linked to user)${NC}"
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/events/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"productName\": \"Premium Subscription\",
    \"amount\": 99.99,
    \"eventType\": \"ORDER_CREATED\"
  }")

echo "Response: $ORDER_RESPONSE"
ORDER_ID=$(echo $ORDER_RESPONSE | jq -r '.eventId')
echo -e "${YELLOW}Order ID: $ORDER_ID${NC}"

sleep 2

# Test 3: Create Multiple Users
echo -e "\n${GREEN}👥 Test 3: Creating Multiple Users${NC}"
for i in {1..3}; do
  echo "Creating user $i..."
  curl -s -X POST http://localhost:8081/api/events/users \
    -H "Content-Type: application/json" \
    -d "{
      \"username\": \"user_$i\",
      \"email\": \"user$i@example.com\",
      \"eventType\": \"USER_CREATED\"
    }" | jq '.'
  sleep 1
done

# Test 4: Create Multiple Orders
echo -e "\n${GREEN}🛒 Test 4: Creating Multiple Orders${NC}"
PRODUCTS=("Basic Plan" "Pro Plan" "Enterprise Plan")
AMOUNTS=(29.99 79.99 199.99)

for i in {0..2}; do
  echo "Creating order for ${PRODUCTS[$i]}..."
  curl -s -X POST http://localhost:8081/api/events/orders \
    -H "Content-Type: application/json" \
    -d "{
      \"userId\": \"$USER_ID\",
      \"productName\": \"${PRODUCTS[$i]}\",
      \"amount\": ${AMOUNTS[$i]},
      \"eventType\": \"ORDER_CREATED\"
    }" | jq '.'
  sleep 1
done

# Test 5: Update User Event
echo -e "\n${GREEN}🔄 Test 5: Creating User Update Event${NC}"
curl -s -X POST http://localhost:8081/api/events/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe_updated",
    "email": "john.updated@example.com",
    "eventType": "USER_UPDATED"
  }' | jq '.'

sleep 2

# Test 6: Order Completion
echo -e "\n${GREEN}✅ Test 6: Creating Order Completion Event${NC}"
curl -s -X POST http://localhost:8081/api/events/orders \
  -H "Content-Type: application/json" \
  -d "{
    \"userId\": \"$USER_ID\",
    \"productName\": \"Premium Subscription\",
    \"amount\": 99.99,
    \"eventType\": \"ORDER_COMPLETED\"
  }" | jq '.'

sleep 2

echo -e "\n${GREEN}✅ All tests completed!${NC}"
echo ""
echo -e "${BLUE}📊 To view consumer logs:${NC}"
echo "  docker logs -f consumer-service"
echo ""
echo -e "${BLUE}📋 To view Kafka topics:${NC}"
echo "  docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092"
echo ""
echo -e "${BLUE}👀 To view messages in user-events topic:${NC}"
echo "  docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic user-events --from-beginning"
echo ""
echo -e "${BLUE}👀 To view messages in order-events topic:${NC}"
echo "  docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic order-events --from-beginning"
