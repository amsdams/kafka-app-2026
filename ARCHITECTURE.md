# Architecture Diagram

## System Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Docker Network                              │
│                                                                     │
│                                                                   │
│  ┌──────────────────────────────────────┼───────────────────────┐ │
│  │         Kafka Broker                 │                       │ │
│  │  Internal: 9092                      │                       │ │
│  │  External: 29092                     │                       │ │
│  │                                      │                       │ │
│  │  Topics:                             │                       │ │
│  │  └─ user-events (3 partitions)       │                       │ │
│  └──────────────────────────────────────┼───────────────────────┘ │
│           ▲                              │              ▲          │
│           │                              │              │          │
│           │ publish                      │              │ consume  │
│           │                              │              │          │
│  ┌────────┴────────┐              ┌─────▼──────┐  ┌───┴─────────┐│
│  │  Producer       │              │ Partition 0│  │  Consumer   ││
│  │  Service        │              ├────────────┤  │  Service    ││
│  │                 │              │ Partition 1│  │             ││
│  │  Spring Boot    │              ├────────────┤  │ Spring Boot ││
│  │  Port: 8081     │              │ Partition 2│  │ Port: 8082  ││
│  │                 │              └────────────┘  │             ││
│  │  REST API       │                              │ 3 Consumers ││
│  │  /api/producer    │                              │ (Concurrent)││
│  └─────────────────┘                              └─────────────┘│
│           ▲                                              │        │
└───────────┼──────────────────────────────────────────────┼────────┘
            │                                              │
            │ HTTP POST                                    │ Process
            │                                              │ & Store
            │                                              ▼
    ┌───────┴────────┐                          ┌──────────────────┐
    │   External     │                          │  Business Logic  │
    │   Client       │                          │  - Logging       │
    │   (curl/app)   │                          │  - DB Save       │
    └────────────────┘                          │  - Notifications │
                                                 └──────────────────┘
```

## Message Flow

```
1. Client Request
   └─► POST /api/producer/publish
       └─► Producer Service receives JSON

2. Producer Processing
   └─► Validate & enrich message
       └─► Add UUID and timestamp
           └─► Serialize to JSON

3. Kafka Publishing
   └─► Send to "user-events" topic
       └─► Kafka assigns partition (0, 1, or 2)
           └─► Message persisted to disk

4. Consumer Polling
   └─► 3 concurrent consumers poll topic
       └─► Each consumer assigned partitions
           └─► Message deserialized from JSON

5. Message Processing
   └─► Business logic executed
       └─► Manual acknowledgment
           └─► Offset committed
```

## Data Model

### UserEvent Structure
```json
{
  "id": "uuid-generated",
  "username": "string",
  "email": "string",
  "eventType": "string",
  "timestamp": "ISO-8601 datetime"
}
```

### Event Types
- USER_CREATED
- USER_UPDATED
- USER_LOGGED_IN
- USER_LOGGED_OUT
- PASSWORD_CHANGED
- (extensible)

## Network Configuration

```
┌────────────────────────────────────────────────────┐
│  kafka-network (Bridge)                            │
│                                                    │
│  Services:                                         │
│  ├─ kafka:9092 (internal)                         │
│  ├─ producer-service:8081                         │
│  └─ consumer-service:8082                         │
│                                                    │
│  External Access:                                  │
│  ├─ localhost:8081 → producer-service:8081        │
│  ├─ localhost:8082 → consumer-service:8082        │
│  └─ localhost:29092 → kafka:9092                  │
└────────────────────────────────────────────────────┘
```

## Scaling Options

### Horizontal Scaling
```
Producer Service:
├─ Instance 1 (8081)
├─ Instance 2 (8083)
└─ Instance 3 (8084)
    └─► Load Balancer

Consumer Service:
├─ Instance 1 (3 consumers) ─► Partitions 0,1,2
├─ Instance 2 (3 consumers) ─► Partitions 3,4,5
└─ Instance 3 (3 consumers) ─► Partitions 6,7,8
    └─► 9 partitions total, 9 concurrent consumers
```

### Vertical Scaling
```
Resources per Service:
├─ CPU: 2+ cores
├─ Memory: 2GB+
└─ Storage: Based on message retention
```

## Security Layers (Future Enhancement)

```
┌────────────────────────────────────┐
│  API Gateway (Future)              │
│  ├─ Authentication                 │
│  ├─ Rate Limiting                  │
│  └─ SSL/TLS                        │
└────────────┬───────────────────────┘
             │
┌────────────▼───────────────────────┐
│  Kafka Security (Future)           │
│  ├─ SASL Authentication            │
│  ├─ ACL Authorization              │
│  └─ Encryption in Transit          │
└────────────────────────────────────┘
```
