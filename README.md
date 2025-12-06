# Java Technologies — Master's Year 1

This repository is used for the course **Java Technologies**, Master's Year 1, and contains my progress on the labs.
**Author:** Stoinea Maria-Miruna

---

## 📘 List of Labs

> **Note:** Labs **1–6** are not included in this file as they were presented physically during the lab sessions.

* [Lab 7 – Messaging with Kafka](#lab-7--messaging-with-kafka)
* [Lab 8 - Microservices](#lab-8---microservices)

---


# Lab 7 – Messaging with Kafka

## 🎯 Overview
This lab extends the PrefSchedule project by implementing a distributed student-grade processing system using **Apache Kafka** as a message broker.  
The system consists of four microservices forming a processing pipeline:

**QuickGrade → Enricher → CourseEnricher → PrefSchedule**

---

# ✅ Implemented Requirements

## Compulsory (1p) – ✔️ Completed

### 1. Kafka Installation and Setup
- Kafka 3.9.1 running locally on Windows  
- Broker available at `localhost:9092`  
- Topics created with multiple partitions for scalability  

### 2. QuickGrade Project
- Spring Boot microservice dedicated to publishing grade events  
- Port: **8081**  
- Publishes:  
  `GradeEvent(studentCode, courseCode, grade)`  
  into `raw-grades-topic`  

### 3. PrefSchedule Consumes and Displays Messages
- Kafka consumer using `@KafkaListener`  
- Console output example:  
  ```
  📩 Received: FullGradeEvent{...}
  ```

Relevant files:
- `QuickGrade/.../GradeProducer.java`
- `QuickGrade/.../KafkaProducerConfig.java`
- `PrefSchedule/.../GradeListener.java`

---

## Homework (2p) – ✔️ Completed

### 1. Database Table for Grades
```sql
CREATE TABLE student_grade (
    id BIGSERIAL PRIMARY KEY,
    student_code VARCHAR(255),
    course_code VARCHAR(255),
    grade DOUBLE PRECISION
);
```

### 2. Save Only Compulsory Courses
```java
if (isCompulsory) {
    StudentGrade sg = new StudentGrade();
    sg.setStudentCode(event.getStudentCode());
    sg.setCourseCode(event.getCourseCode());
    sg.setGrade(event.getGrade());
    repo.save(sg);
}
```

Ignored optional courses log:
```
⏭️ Skipped optional course
```

### 3. REST Endpoints
**GET /grades**
```
curl http://localhost:8080/grades
```

**POST /grades/upload**
```
curl -X POST http://localhost:8080/grades/upload -F "file=@grades.csv"
```

CSV:
```
STU001,COMP101,9.5
STU002,COMP102,8.7
```

### 4. Dead-Letter Queue (DLQ)
- DLQ topic: `grades_topic.DLT`  
- Retry logic: **3 retries, 1s delay**  
- Handled by `DeadLetterListener.java`

Test message:
```json
{
  "studentCode": "ST9999",
  "courseCode": "INVALID_COURSE",
  "grade": 7.0
}
```

Expected:
1. Three retries  
2. Message moved to DLQ  
3. Log:  
   ```
   ⚠️ Message moved to DLQ: {...}
   ```

---

# Advanced (2p) – ✔️ Completed

## 1. Multi-Stage Processing Pipeline

### Architecture
```
┌─────────────┐    raw-grades-topic     ┌──────────┐
│ QuickGrade  │ ──────────────────────> │ Enricher │
│   (8081)    │   GradeEvent            │  (8082)  │
└─────────────┘                         └──────────┘
       │                                     │
       │ enriched-grades-topic               │
       ▼                                     ▼
                                  ┌────────────────┐
                                  │ CourseEnricher │
                                  │    (8083)      │
                                  └────────────────┘
                                          │
                                          │ grades_topic
                                          ▼
                                  ┌──────────────┐
                                  │ PrefSchedule │
                                  │   (8080)     │
                                  └──────────────┘
```

### Pipeline Components
#### QuickGrade
Publishes `GradeEvent` with:  
- studentCode  
- courseCode  
- grade  

#### Enricher
- Consumes from `raw-grades-topic`  
- Fetches: `studentName`, `year`  
- Publishes `EnrichedGradeEvent`  

#### CourseEnricher
- Consumes from `enriched-grades-topic`  
- Fetches: `courseName`, `semester`  
- Publishes `FullGradeEvent`  

#### PrefSchedule
- Consumes fully enriched events  
- Saves only compulsory courses  

---

## 2. Advanced Kafka Features

### A. Partitions & Consumer Groups
Topics created with **3 partitions**:
```
kafka-topics --create --topic raw-grades-topic --partitions 3 --replication-factor 1
kafka-topics --create --topic enriched-grades-topic --partitions 3
kafka-topics --create --topic grades_topic --partitions 3
```

Consumer concurrency:
```java
factory.setConcurrency(3);
```

Benefits:
- Increased throughput  
- Automatic load balancing  
- Horizontal scalability  

---

### B. Exactly-Once Semantics

Producer:
```java
config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
config.put(ProducerConfig.ACKS_CONFIG, "all");
config.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
```

Consumer:
```java
config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
```

Guarantees:
- No duplicates  
- No message loss  
- Partition order preserved  

---

### C. Observable Kafka Metrics

Logs include:
- Idempotence status  
- Partition assignments  
- Throughput indicators  
- Consumer group coordination  
- Latency and lag  

---

# Running the System

1. Start Kafka  
2. Create topics  
3. Start services in this order:
   - Enricher  
   - CourseEnricher  
   - PrefSchedule  
   - QuickGrade  
4. Publish sample events or upload CSVs  

---

# Expected Demo Output

## Valid Grade Flow

### QuickGrade
```
Produced event = GradeEvent{studentCode='STU001', courseCode='COMP101', grade=9.5}
```

### Enricher
```
📥 Enricher received: GradeEvent{...}
📤 Enricher sent: EnrichedGradeEvent{...}
```

### CourseEnricher
```
📥 CourseEnricher received: EnrichedGradeEvent{...}
📤 CourseEnricher sent: FullGradeEvent{...}
```

### PrefSchedule
```
📩 Received: FullGradeEvent{...}
✅ Saved compulsory grade
```

---

## DLQ Scenario (Invalid Course)
```
📩 Received: FullGradeEvent{..., courseCode='INVALID', ...}
❌ Course not found: INVALID
[retry 1/3]
[retry 2/3]
[retry 3/3]
⚠️ Message moved to DLQ: FullGradeEvent{...}
```

---

# Evaluation Summary

| Section     | Points | Status        |
|------------|--------|---------------|
| Compulsory | 1p     | ✔️ Completed |
| Homework   | 2p     | ✔️ Completed |
| Advanced   | 2p     | ✔️ Completed |

---

# Useful Kafka Commands

```
# List topics
kafka-topics --list --bootstrap-server localhost:9092

# Describe topic
kafka-topics --describe --topic grades_topic --bootstrap-server localhost:9092

# Consume messages
kafka-console-consumer --topic grades_topic --from-beginning --bootstrap-server localhost:9092

# List consumer groups
kafka-consumer-groups --list --bootstrap-server localhost:9092

# Describe lag
kafka-consumer-groups --describe --group prefschedule-group --bootstrap-server localhost:9092

# Delete topic
kafka-topics --delete --topic grades_topic --bootstrap-server localhost:9092
```

---

# Technologies
- Apache Kafka 3.9.1  
- Spring Boot 3.x  
- Spring Kafka  
- PostgreSQL  
- Jackson  
- Lombok  

# Lab 8 - Microservices
## 🎯 Overview

This lab extends the PrefSchedule ecosystem by introducing a **StableMatch microservice** responsible for matching students to courses based on preferences and grades. The lab demonstrates **resilient microservice communication** between PrefSchedule and StableMatch using WebClient, with Resilience4j patterns applied to ensure fault tolerance.

---

## Implementation Status: ✅ Complete

### Compulsory (1p) ✅

**StableMatch Microservice**
- Created independent Spring Boot project on port 8084
- Implemented REST controller with stable matching endpoint
- Designed MatchingRequestDTO and MatchingResponseDTO with proper validation
- JSON-based request/response format for matching problems

### Homework (2p) ✅

**Instructor Preferences**
- Created `instructor_course_preferences` table with foreign key to courses
- Implemented entity, repository, service, and controller layers
- REST endpoints for CRUD operations on instructor preferences
- Weighted grade calculation based on compulsory course performance

**StableMatch REST Endpoints**
- `GET /api/matching/assignments` - retrieve all assignments
- `GET /api/matching/assignments/student/{code}` - get assignment for specific student
- `GET /api/matching/assignments/course/{code}` - get assignments for specific course
- `GET /api/matching/statistics` - retrieve matching statistics

**Matching Algorithms**
- Implemented random matching algorithm (shuffle-based assignment)
- Stable matching algorithm using Gale-Shapley approach

**Resilience Patterns (Basic)**
- Retry: 3 attempts with exponential backoff (1s, 2s, 4s)
- Fallback: Stable → Random → Empty response chain
- Timeout: 30-second limit on service calls

**Service Integration**
- PrefSchedule invokes StableMatch via WebClient
- MatchingOrchestrationService coordinates matching for packs
- Support for both stable and random algorithms via configuration

### Advanced (2p) ✅

**Gale-Shapley Algorithm**
- Complete implementation of stable matching algorithm
- Student proposal phase with preference ordering
- Course selection based on instructor preferences and student scores
- Weighted grade calculation for student ranking
- Guarantees stability in matching results

**Advanced Resilience Patterns**
- **CircuitBreaker**: Opens after 50% failure rate (5/10 requests), 5s recovery window
- **Bulkhead**: Limits concurrent calls to 5, prevents service overload
- **RateLimiter**: 10 requests/second limit with automatic rejection
- All patterns configured via Resilience4j with health indicators

**Failure Simulation & Testing**
- ResilienceTestController endpoints for simulating failures, delays, and timeouts
- ResilienceMonitoringController for real-time pattern state inspection
- Configurable failure modes: complete failure, random failures, slow responses

**JMeter Stress Testing**
- Test plan with 5 scenarios covering all resilience patterns
- Rate Limiter Test: 20 concurrent requests in 1 second
- Circuit Breaker Test: 10 failing requests to trigger opening
- Bulkhead Test: 10 concurrent requests with 5-second delays
- Timeout Test: 35-second requests exceeding 30s limit
- Combined Stress Test: 50 threads for 60 seconds with random failures
- Test results saved in `JMeter_StressTests/` folder (all tests passed)

## Architecture

### StableMatch Service (Port 8084)
- Independent microservice with no PrefSchedule dependencies
- Matching algorithms (random and Gale-Shapley)
- Test endpoints for resilience pattern demonstration

### PrefSchedule Service (Port 8080)
- Instructor preference management
- StableMatchClient with full resilience patterns
- Orchestration service for pack-level matching
- Real-time monitoring endpoints

## Technologies Used
- Spring Boot 3.2.0
- Spring WebFlux (WebClient)
- Resilience4j (CircuitBreaker, Retry, RateLimiter, Bulkhead, TimeLimiter)
- JMeter 5.6.3 for load testing
- PostgreSQL for data persistence
- Lombok for boilerplate reduction

## Testing & Monitoring

### Resilience Pattern Verification
```bash
# Circuit Breaker state
curl http://localhost:8080/api/resilience/monitor/circuit-breaker/stableMatchService

# Rate Limiter state
curl http://localhost:8080/api/resilience/monitor/rate-limiter/stableMatchService

# Bulkhead state
curl http://localhost:8080/api/resilience/monitor/bulkhead/stableMatchService

# All patterns overview
curl http://localhost:8080/api/resilience/monitor/all
```