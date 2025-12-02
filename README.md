# 📚 Lab 7 – Messaging with Kafka

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

# ⭐ Advanced (2p) – ✔️ Completed

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

# 🚀 Running the System

1. Start Kafka  
2. Create topics  
3. Start services in this order:
   - Enricher  
   - CourseEnricher  
   - PrefSchedule  
   - QuickGrade  
4. Publish sample events or upload CSVs  

---

# 📊 Expected Demo Output

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

# 🎯 Evaluation Summary

| Section     | Points | Status        |
|------------|--------|---------------|
| Compulsory | 1p     | ✔️ Completed |
| Homework   | 2p     | ✔️ Completed |
| Advanced   | 2p     | ✔️ Completed |

---

# 📁 Useful Kafka Commands

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

# 🔧 Technologies
- Apache Kafka 3.9.1  
- Spring Boot 3.x  
- Spring Kafka  
- PostgreSQL  
- Jackson  
- Lombok  
