# CQRS Pattern with Polyglot Persistence - E-Commerce Order System

This project implements **CQRS (Command Query Responsibility Segregation)** with **Polyglot Persistence** and the **Transactional Outbox Pattern** for managing read and write operations across multiple microservices in an e-commerce system. The implementation demonstrates separation of command and query models, eventual consistency, and event-driven architecture.

## 🏗️ Architecture

### Microservices Architecture
The system consists of 4 independent Spring Boot microservices:

| Service | Port | Database | Type | Description |
|---------|------|----------|------|-------------|
| **Order Service** | 8081 | PostgreSQL | Command | Handles order creation (write model) |
| **Product Service** | 8082 | PostgreSQL | Command | Manages product catalog (write model) |
| **User Service** | 8083 | MySQL | Command | Manages user data (write model) |
| **Order History Service** | 8084 | MongoDB | Query | Provides read-optimized order history |

### CQRS Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    COMMAND SIDE (Write Model)                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐   │
│  │Order Service │     │Product Service│    │ User Service │   │
│  │ PostgreSQL   │     │  PostgreSQL   │    │    MySQL     │   │
│  │  Port 8081   │     │   Port 8082   │    │  Port 8083   │   │
│  └──────┬───────┘     └──────┬────────┘    └──────┬───────┘   │
│         │                    │                     │           │
│         │ OrderEvent         │ ProductEvent        │ UserEvent │
│         └────────────────────┼─────────────────────┘           │
│                              │                                 │
└──────────────────────────────┼─────────────────────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │   Apache Kafka      │
                    │   Message Broker    │
                    │   Port 9092         │
                    └──────────┬──────────┘
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
   ┌────────▼────────┐  ┌─────▼──────┐  ┌───────▼────────┐
   │  order-events   │  │product-    │  │  user-events   │
   │  (Kafka Topic)  │  │events      │  │  (Kafka Topic) │
   └────────┬────────┘  └─────┬──────┘  └───────┬────────┘
            │                 │                  │
            └─────────────────┼──────────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────────┐
│                     QUERY SIDE (Read Model)                   │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│              ┌──────────────────────────────┐                 │
│              │  Order History Service       │                 │
│              │  MongoDB (Denormalized)      │                 │
│              │  Port 8084                   │                 │
│              └──────────────────────────────┘                 │
│                                                               │
│   Stores: Order + User Info + Product Info                   │
│   Single query returns complete order history                │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

### Transactional Outbox Flow

```
Order Service Transaction:
┌──────────────────────────────────────┐
│  1. INSERT INTO orders               │
│  2. INSERT INTO outbox_events        │
│     ↓ (Same Database Transaction)    │
│  3. COMMIT                           │
└──────────────────────────────────────┘
            ↓
┌──────────────────────────────────────┐
│  Outbox Processor (Polling @5s)      │
│  1. SELECT * FROM outbox_events      │
│     WHERE published = false          │
│  2. PUBLISH to Kafka                 │
│  3. UPDATE published = true          │
└──────────────────────────────────────┘
```

## 🎯 Pattern Implementation

### 1. CQRS (Command Query Responsibility Segregation)

**Command Side (Write Operations):**
- Order Service: Creates orders
- Product Service: Manages products
- User Service: Manages users
- Each service has its own database
- Optimized for transactional consistency

**Query Side (Read Operations):**
- Order History Service: Denormalized read model
- Single MongoDB database
- Optimized for fast queries
- Pre-aggregated data from all command services

**Benefits:**
- ✅ Independent scaling of reads and writes
- ✅ Optimized data models for different access patterns
- ✅ No complex joins at query time
- ✅ Better performance for both operations

### 2. Polyglot Persistence

Different databases for different needs:

| Service | Database | Reason |
|---------|----------|--------|
| Order Service | PostgreSQL | Strong ACID guarantees for orders |
| Product Service | PostgreSQL | Relational product catalog |
| User Service | MySQL | Different SQL engine (demonstrates flexibility) |
| Order History | MongoDB | Fast document queries, flexible schema |

**Benefits:**
- ✅ Use best database for each service's needs
- ✅ No vendor lock-in
- ✅ Services can evolve independently
- ✅ Demonstrates real-world polyglot architecture

### 3. Transactional Outbox Pattern

Ensures **atomicity** between database updates and event publishing:

**Problem:** How to update database AND publish event reliably?

**Solution:**
1. **Outbox Table**: Dedicated table for events
2. **Atomic Write**: Business data + outbox event in same transaction
3. **Message Relay**: Background processor publishes events from outbox
4. **Guaranteed Delivery**: Events persisted before publishing

**Implementation:**
- Scheduler runs every 5 seconds
- Polls for unpublished events
- Publishes to Kafka
- Marks as published
- Retries on failure

**Benefits:**
- ✅ No lost messages
- ✅ No distributed transactions (2PC)
- ✅ Database and message broker always consistent
- ✅ Simple and reliable

## 🧪 Testing & Results

### Test 1: Create User ✅

**Request:**
```http
POST http://localhost:8083/api/users
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": "123 Main St, Anytown"
}
```

**Response:**
```json
{
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": "123 Main St, Anytown"
}
```

**Database (MySQL):**
```sql
SELECT * FROM users;
+----+----------+---------------------+-------------------+
| id | name     | email               | address           |
+----+----------+---------------------+-------------------+
| 1  | John Doe | john.doe@example.com| 123 Main St, ...  |
+----+----------+---------------------+-------------------+
```

**Outbox Table:**
```sql
SELECT * FROM outbox_events;
+----+-------------+-------------+----------+----------+---------------------+
| id | aggregate_id| event_type  | payload  | published| created_at          |
+----+-------------+-------------+----------+----------+---------------------+
| 1  | 1           | USER_CREATED| {...}    | true     | 2026-01-06 23:45:10 |
+----+-------------+-------------+----------+----------+---------------------+
```

---

### Test 2: Create Product ✅

**Request:**
```http
POST http://localhost:8082/api/products
Content-Type: application/json

{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1200.00,
    "stock": 50
}
```

**Response:**
```json
{
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1200.0,
    "stock": 50
}
```

**Database (PostgreSQL):**
```sql
SELECT * FROM products;
+----+--------+---------------------------+--------+-------+
| id | name   | description               | price  | stock |
+----+--------+---------------------------+--------+-------+
| 1  | Laptop | High-performance laptop   | 1200.0 | 50    |
+----+--------+---------------------------+--------+-------+
```

---

### Test 3: Create Order ✅

**Request:**
```http
POST http://localhost:8081/api/orders
Content-Type: application/json

{
    "userId": 1,
    "productId": 1,
    "quantity": 2,
    "totalPrice": 2400.00
}
```

**Response:**
```json
{
    "id": 1,
    "userId": 1,
    "productId": 1,
    "quantity": 2,
    "totalPrice": 2400.0,
    "status": "CREATED",
    "orderDate": "2026-01-06T23:50:15.123"
}
```

**Console Output (Order Service):**
```
INFO: Saved order to database: Order(id=1, userId=1, productId=1, ...)
INFO: Saved event to outbox: OutboxEvent(id=1, eventType=ORDER_CREATED, ...)
INFO: Published event: ORDER_CREATED for aggregate: 1
```

---

### Test 4: Query Order History (CQRS Read Model) 🔍

**Wait 10 seconds for event processing...**

**Request:**
```http
GET http://localhost:8084/api/order-history
```

**Response:**
```json
[
    {
        "id": "677c8a1b2f4d3a001e5b2c1a",
        "orderId": 1,
        "userId": 1,
        "userName": "John Doe",
        "userEmail": "john.doe@example.com",
        "productId": 1,
        "productName": "Laptop",
        "productPrice": 1200.0,
        "quantity": 2,
        "totalPrice": 2400.0,
        "status": "CREATED",
        "orderDate": "2026-01-06T23:50:15.123",
        "createdAt": "2026-01-06T23:50:20.456"
    }
]
```

**What This Shows:**
- ✅ **Denormalized data**: User name, email, product name all in one document
- ✅ **No joins needed**: Single query returns complete information
- ✅ **Fast queries**: MongoDB optimized for reads
- ✅ **Eventual consistency**: ~5-10 second delay acceptable

**MongoDB Data:**
```javascript
db.order_history.find().pretty()
{
    "_id": ObjectId("677c8a1b2f4d3a001e5b2c1a"),
    "orderId": 1,
    "userId": 1,
    "userName": "John Doe",          // ← From User Service
    "userEmail": "john.doe@example.com",
    "productId": 1,
    "productName": "Laptop",         // ← From Product Service
    "productPrice": 1200.0,
    "quantity": 2,
    "totalPrice": 2400.0,
    "status": "CREATED",
    "orderDate": ISODate("2026-01-06T23:50:15.123Z"),
    "createdAt": ISODate("2026-01-06T23:50:20.456Z")
}
```

---

### Test 5: Query by User ID 🔍

**Request:**
```http
GET http://localhost:8084/api/order-history/user/1
```

**Response:**
```json
[
    {
        "id": "677c8a1b2f4d3a001e5b2c1a",
        "orderId": 1,
        "userId": 1,
        "userName": "John Doe",
        "userEmail": "john.doe@example.com",
        "productId": 1,
        "productName": "Laptop",
        "productPrice": 1200.0,
        "quantity": 2,
        "totalPrice": 2400.0,
        "status": "CREATED",
        "orderDate": "2026-01-06T23:50:15.123",
        "createdAt": "2026-01-06T23:50:20.456"
    }
]
```

**Performance:**
- Fast indexed query on `userId`
- No network calls to other services
- No joins across databases
- Sub-millisecond response time

---

### Test 6: Query by Order ID 🔍

**Request:**
```http
GET http://localhost:8084/api/order-history/order/1
```

**Response:**
```json
{
    "id": "677c8a1b2f4d3a001e5b2c1a",
    "orderId": 1,
    "userId": 1,
    "userName": "John Doe",
    "userEmail": "john.doe@example.com",
    "productId": 1,
    "productName": "Laptop",
    "productPrice": 1200.0,
    "quantity": 2,
    "totalPrice": 2400.0,
    "status": "CREATED",
    "orderDate": "2026-01-06T23:50:15.123",
    "createdAt": "2026-01-06T23:50:20.456"
}
```

---

## 🔑 Key Features Demonstrated

### 1. CQRS Pattern ✅
- Separate command and query models
- Independent scaling
- Optimized for different access patterns
- Clear separation of concerns

### 2. Polyglot Persistence ✅
- PostgreSQL for orders and products
- MySQL for users
- MongoDB for read model
- Right database for each job

### 3. Transactional Outbox ✅
- Atomic database + event writes
- No lost messages
- Reliable event publishing
- No distributed transactions

### 4. Event-Driven Architecture ✅
- Apache Kafka message broker
- Asynchronous communication
- Loose coupling between services
- Scalable event streaming

### 5. Denormalization ✅
- Pre-aggregated data in read model
- Fast queries (no joins)
- Single service call for complete data
- Trade-off: storage for performance

### 6. Eventual Consistency ✅
- ~5-10 second delay for read model updates
- Acceptable for most business cases
- Better availability and scalability
- Clear consistency guarantees

## 📈 Benefits Over Traditional Monolith

| Aspect | Monolithic Database | CQRS + Polyglot (This Implementation) |
|--------|---------------------|---------------------------------------|
| **Read Performance** | ❌ Slow (complex joins) | ✅ Fast (denormalized) |
| **Write Performance** | ⚠️ Moderate | ✅ Fast (no read concerns) |
| **Scalability** | ❌ Limited (single DB) | ✅ Independent scaling |
| **Database Choice** | ❌ Single type | ✅ Best for each service |
| **Schema Evolution** | ❌ Coupled | ✅ Independent |
| **Availability** | ❌ Single point of failure | ✅ Service isolation |
| **Consistency** | ✅ Immediate | ⚠️ Eventual (~5-10s) |

## 🛡️ Reliability Guarantees

### Transactional Outbox Guarantees:
1. **At-Least-Once Delivery**: Events may be published multiple times but never lost
2. **Ordering**: Events processed in creation order
3. **Atomicity**: Database and outbox always consistent
4. **Recoverability**: Failed publishes retried automatically

### Idempotency:
- Event listeners check for duplicate processing
- MongoDB upserts prevent duplicate documents
- Safe to replay events

### Error Handling:
- Failed events remain in outbox for retry
- Detailed logging for debugging
- Service isolation (one failure doesn't cascade)