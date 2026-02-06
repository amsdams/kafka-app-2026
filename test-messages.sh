#!/bin/bash

echo "📤 Sending test messages to Kafka..."
echo ""

# Array of sample events
declare -a users=("alice" "bob" "charlie" "diana" "eve")
declare -a events=("USER_CREATED" "USER_UPDATED" "USER_LOGGED_IN" "USER_LOGGED_OUT" "PASSWORD_CHANGED")

# Send 10 test messages
for i in {1..10}; do
    # Random user and event type
    user=${users[$RANDOM % ${#users[@]}]}
    event=${events[$RANDOM % ${#events[@]}]}
    
    echo "[$i/10] Sending event: $event for user: $user"
    
    response=$(curl -s -X POST http://localhost:8081/api/events/publish \
      -H "Content-Type: application/json" \
      -d "{
        \"username\": \"$user\",
        \"email\": \"${user}@example.com\",
        \"eventType\": \"$event\"
      }")
    
    echo "Response: $response"
    echo ""
    
    sleep 1
done

echo "✅ Finished sending test messages!"
echo ""
echo "📋 Check consumer logs with:"
echo "   docker logs -f consumer-service"
