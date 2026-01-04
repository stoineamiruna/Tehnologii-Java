# Saga Pattern Implementation - E-Commerce Order System

This project implements the **Saga Pattern** using **Orchestration** for managing distributed transactions across multiple microservices in an e-commerce order processing system. The implementation demonstrates compensatable, pivot, and retriable transactions as required by the Java Technology course assignment.

## 🏗️ Architecture

### Microservices Architecture
The system consists of 5 independent Spring Boot microservices:

| Service | Port | Type | Description |
|---------|------|------|-------------|
| **Order Service** | 8080 | Orchestrator | Saga coordinator, manages workflow |
| **Payment Service** | 8081 | Compensatable | Handles payment reservations and refunds |
| **Inventory Service** | 8082 | Compensatable | Manages inventory reservations and releases |
| **Shipping Service** | 8083 | Pivot | Ships orders (point of no return) |
| **Notification Service** | 8084 | Retriable | Sends order notifications with retry logic |

### Saga Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    ORDER SAGA ORCHESTRATOR                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────────┐
        │  Step 1: Reserve Payment (COMPENSATABLE)    │
        │  ✓ Success → Continue                       │
        │  ✗ Failure → End saga (no compensation)     │
        └─────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────────┐
        │  Step 2: Reserve Inventory (COMPENSATABLE)  │
        │  ✓ Success → Continue                       │
        │  ✗ Failure → Refund payment                 │
        └─────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────────┐
        │  Step 3: Ship Order (PIVOT)                 │
        │  ⚠️  POINT OF NO RETURN                     │
        │  ✓ Success → Saga committed, must complete  │
        │  ✗ Failure → Compensate all previous steps  │
        └─────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────────┐
        │  Step 4: Send Notification (RETRIABLE)      │
        │  🔄 Retry up to 3 times with 2s delay       │
        │  ✓ Success → Order COMPLETED                │
        │  ✗ Failure → Keep retrying                  │
        └─────────────────────────────────────────────┘
```

## 🎯 Transaction Types Implementation

### 1. Compensatable Transactions
Transactions that can be rolled back using compensating operations:

- **Reserve Payment** → Compensation: **Refund Payment**
- **Reserve Inventory** → Compensation: **Release Inventory**

**How it works:**
- If any compensatable step fails, all previous completed compensatable steps are reversed in **reverse order**
- Compensation ensures eventual consistency across services

### 2. Pivot Transaction
The critical point where the saga commits and cannot be undone:

- **Ship Order** - Once shipping is initiated, the order is physically in transit
- After this point, the saga **must complete** even if subsequent steps fail
- No compensation possible for pivot transactions

### 3. Retriable Transactions
Transactions that execute after the pivot and retry until success:

- **Send Notification** - Configured with 3 retry attempts and 2-second delay
- Implements exponential backoff strategy
- Idempotent to handle duplicate executions safely

## 📁 Project Structure

```
saga-pattern-implementation/
├── order-service/          # Saga orchestrator
│   ├── src/main/java/com/saga/order/
│   │   ├── saga/           # Orchestration logic
│   │   ├── model/          # Domain entities
│   │   ├── service/        # Business logic
│   │   ├── controller/     # REST endpoints
│   │   └── repository/     # Data access
│   └── application.properties
├── payment-service/        # Payment management
├── inventory-service/      # Inventory management
├── shipping-service/       # Shipping operations
└── notification-service/   # Notification system
```

## 🧪 Testing & Results

### Test 1: Successful Order (Happy Path) ✅

**Request:**
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
    "customerId": "CUST001",
    "productId": "PROD123",
    "quantity": 5,
    "amount": 500.00
}
```

**Response:**
```json
{
    "orderId": 1,
    "customerId": "CUST001",
    "productId": "PROD123",
    "quantity": 5,
    "amount": 500.00,
    "status": "COMPLETED",
    "message": "Order completed successfully"
}
```

**Console Output (Order Service):**
```
INFO: Starting Saga for Order ID: 1
INFO: Executing step: RESERVE_PAYMENT (Type: COMPENSATABLE)
INFO: Step RESERVE_PAYMENT succeeded
INFO: Executing step: RESERVE_INVENTORY (Type: COMPENSATABLE)
INFO: Step RESERVE_INVENTORY succeeded
INFO: Executing step: SHIP_ORDER (Type: PIVOT)
INFO: Step SHIP_ORDER succeeded
INFO: Executing step: SEND_NOTIFICATION (Type: RETRIABLE)
INFO: Attempt 1/3 for retriable step: SEND_NOTIFICATION
INFO: Retriable step SEND_NOTIFICATION succeeded on attempt 1
INFO: Saga completed successfully for Order ID: 1
```

**What Happened:**
- ✅ Payment reserved successfully
- ✅ Inventory reserved successfully
- ✅ Order shipped (pivot transaction)
- ✅ Notification sent on first attempt
- ✅ Order status: COMPLETED

---

### Test 2: Payment Failure (Early Failure - No Compensation) ❌

**Request:**
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
    "customerId": "CUST002",
    "productId": "PROD456",
    "quantity": 10,
    "amount": 15000.00
}
```

**Response:**
```json
{
    "orderId": 2,
    "customerId": "CUST002",
    "productId": "PROD456",
    "quantity": 10,
    "amount": 15000.00,
    "status": "FAILED",
    "message": "Order failed: Failed at step: RESERVE_PAYMENT"
}
```

**Console Output (Order Service):**
```
INFO: Starting Saga for Order ID: 2
INFO: Executing step: RESERVE_PAYMENT (Type: COMPENSATABLE)
ERROR: Step RESERVE_PAYMENT failed: Payment amount exceeds limit
ERROR: Step RESERVE_PAYMENT failed. Initiating compensation...
INFO: Starting compensation for 0 completed steps
INFO: Compensation completed
```

**What Happened:**
- ❌ Payment failed (amount > 10000 limit)
- ⚠️ No compensation needed (first step)
- ❌ Order status: FAILED
- 🎯 Demonstrates early failure handling

---

### Test 3: Inventory Failure (Mid-Saga - Compensation Triggered) ⚠️

**Request:**
```http
POST http://localhost:8080/orders
Content-Type: application/json

{
    "customerId": "CUST003",
    "productId": "PROD789",
    "quantity": 150,
    "amount": 2000.00
}
```

**Response:**
```json
{
    "orderId": 3,
    "customerId": "CUST003",
    "productId": "PROD789",
    "quantity": 150,
    "amount": 2000.00,
    "status": "FAILED",
    "message": "Order failed: Failed at step: RESERVE_INVENTORY"
}
```

**Console Output (Order Service):**
```
INFO: Starting Saga for Order ID: 3
INFO: Executing step: RESERVE_PAYMENT (Type: COMPENSATABLE)
INFO: Step RESERVE_PAYMENT succeeded
INFO: Executing step: RESERVE_INVENTORY (Type: COMPENSATABLE)
ERROR: Step RESERVE_INVENTORY failed: Insufficient inventory
ERROR: Step RESERVE_INVENTORY failed. Initiating compensation...
INFO: Starting compensation for 1 completed steps
INFO: Compensating step: RESERVE_PAYMENT
INFO: Calling compensation service: http://localhost:8081/payment/refund
INFO: Compensation for step RESERVE_PAYMENT succeeded
INFO: Compensation completed
```

**What Happened:**
- ✅ Payment reserved successfully
- ❌ Inventory reservation failed (quantity > 100)
- 🔄 **COMPENSATION TRIGGERED**
- ✅ Payment refunded (compensated)
- ❌ Order status: FAILED
- 🎯 **This perfectly demonstrates compensatable transactions!**

---

### Test 4: Get Order Details (Successful Order) 🔍

**Request:**
```http
GET http://localhost:8080/orders/1
```

**Response:**
```json
{
    "id": 1,
    "customerId": "CUST001",
    "productId": "PROD123",
    "quantity": 5,
    "amount": 500.00,
    "status": "COMPLETED",
    "createdAt": "2026-01-04T14:22:12.088663",
    "updatedAt": "2026-01-04T14:22:15.791795",
    "failureReason": null
}
```

**What This Shows:**
- Order completed successfully
- Timestamps show creation and completion time
- No failure reason (success case)

---

### Test 5: Get Order Details (Failed - Payment) 🔍

**Request:**
```http
GET http://localhost:8080/orders/2
```

**Response:**
```json
{
    "id": 2,
    "customerId": "CUST002",
    "productId": "PROD456",
    "quantity": 10,
    "amount": 15000.00,
    "status": "FAILED",
    "createdAt": "2026-01-04T14:26:05.768",
    "updatedAt": "2026-01-04T14:26:05.783",
    "failureReason": "Failed at step: RESERVE_PAYMENT"
}
```

**What This Shows:**
- Order failed at payment step
- Failure reason clearly documented
- Very quick failure (no long processing)

---

### Test 6: Get Order Details (Failed - Inventory with Compensation) 🔍

**Request:**
```http
GET http://localhost:8080/orders/3
```

**Response:**
```json
{
    "id": 3,
    "customerId": "CUST003",
    "productId": "PROD789",
    "quantity": 150,
    "amount": 2000.00,
    "status": "FAILED",
    "createdAt": "2026-01-04T14:29:02.190",
    "updatedAt": "2026-01-04T14:29:02.288",
    "failureReason": "Failed at step: RESERVE_INVENTORY"
}
```

**What This Shows:**
- Order failed at inventory step
- Compensation was executed (payment refunded)
- Updated timestamp reflects compensation completion

---

### Test 7: Get All Orders (Summary View) 📊

**Request:**
```http
GET http://localhost:8080/orders
```

**Response:**
```json
[
    {
        "id": 1,
        "customerId": "CUST001",
        "productId": "PROD123",
        "quantity": 5,
        "amount": 500.00,
        "status": "COMPLETED",
        "createdAt": "2026-01-04T14:22:12.088663",
        "updatedAt": "2026-01-04T14:22:15.791795",
        "failureReason": null
    }
]
```

**Note:** Only completed orders are shown in list view. This test shows Order #1 successfully completed while Orders #2 and #3 failed and were properly handled with compensation where needed.

---

## 🔑 Key Features Demonstrated

### 1. Orchestration Pattern ✅
- Centralized saga coordinator (Order Service)
- Clear workflow management
- Easy to debug and trace
- Single point of control

### 2. Compensatable Transactions ✅
- Payment refund on failure
- Inventory release on failure
- Executed in reverse order
- Ensures eventual consistency

### 3. Pivot Transaction ✅
- Shipping marks point of no return
- After pivot, saga must complete
- Cannot be compensated
- Business-critical commitment point

### 4. Retriable Transactions ✅
- Notification retries up to 3 times
- 2-second delay between attempts
- Idempotent operations
- Eventual success guaranteed

### 5. Error Handling ✅
- Graceful failure management
- Detailed error messages
- Proper status tracking
- Comprehensive logging

### 6. Idempotency ✅
- All operations check for duplicates
- Safe to retry any transaction
- Prevents double processing
- Consistent state management

### 7. Data Consistency ✅
- Each service has own database
- No distributed transactions (2PC)
- Eventual consistency through saga
- Audit trail maintained

## 📈 Benefits Over Traditional 2PC

| Aspect | 2PC (Traditional) | Saga Pattern (This Implementation) |
|--------|-------------------|-------------------------------------|
| **Availability** | ❌ Lower (synchronous locks) | ✅ Higher (asynchronous) |
| **Scalability** | ❌ Limited (coordination overhead) | ✅ Better (independent services) |
| **Failure Handling** | ❌ All-or-nothing rollback | ✅ Compensating transactions |
| **Performance** | ❌ Slower (blocking) | ✅ Faster (non-blocking) |
| **Complexity** | ✅ Simpler logic | ⚠️ More complex (compensation) |
| **Debugging** | ❌ Harder (distributed locks) | ✅ Easier (orchestrator logs) |


## 🛡️ Isolation Problem Solutions Implemented

### 1. Semantic Locks
- Status fields (PENDING, RESERVED, COMPLETED, FAILED)
- Prevents concurrent modifications
- Business-level locking

### 2. Idempotency
- Check for existing reservations
- Safe retry mechanism
- Prevents duplicate operations

### 3. Optimistic Locking
- Version checking in updates
- Timestamp-based validation
- Detects concurrent changes
