# PrefSchedule - Microservices-Based Student Course Assignment System


## 📋 Table of Contents

- [Overview](#overview)
- [System Architecture](#system-architecture)
- [Microservices](#microservices)
- [Technologies](#technologies)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Monitoring & Observability](#monitoring--observability)
- [Security](#security)
- [Testing](#testing)
- [Deployment](#deployment)
- [Contributing](#contributing)

## 🎯 Overview

**PrefSchedule** is a production-ready, cloud-native microservices system that automatically assigns students to optional courses based on their preferences, course capacity, and instructor preferences. The system ensures fair distribution using the **Gale-Shapley stable matching algorithm** and provides comprehensive observability, security, and fault tolerance.

### Key Capabilities

- **Intelligent Course Assignment**: Stable matching algorithm guarantees optimal student-course pairings
- **Event-Driven Architecture**: Real-time grade processing and matching orchestration via Apache Kafka
- **Cloud-Native Design**: Service discovery, centralized configuration, API Gateway with load balancing
- **Production-Grade Resilience**: Circuit breakers, bulkheads, rate limiting, and retry mechanisms
- **Enterprise Security**: JWT-based authentication, role-based access control, encrypted secrets management
- **Comprehensive Monitoring**: Prometheus metrics, Grafana dashboards, real-time alerting

## 🏗️ System Architecture

### High-Level Architecture

```
                          ┌─────────────────┐
                          │  Eureka Server  │
                          │   (Discovery)   │
                          └────────┬────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
         ┌──────────▼─────┐  ┌─────▼───────┐  ┌───▼────────┐
         │  API Gateway   │  │ Config      │  │ Prometheus │
         │  (Port 8085)   │  │ Server      │  │  Metrics   │
         └───────┬────────┘  │ (Port 8888) │  └─────┬──────┘
                 │           └─────────────┘        │
         ┌───────┴────────────────────────┐         │
         │  Load Balanced Routing         │         │
         └───┬─────────┬──────────┬───────┘         │
             │         │          │                 │
    ┌────────▼──┐ ┌────▼──────┐ ┌─▼──────────┐      │
    │StableMatch│ │StableMatch│ │StableMatch │◄─────┤
    │  :8084    │ │  :8086    │ │  :8087     │      │
    └─────┬─────┘ └────┬──────┘ └─────┬──────┘      │
          │            │              │             │
          └────────────┴──────────────┘             │
                       │                            │
              ┌────────▼────────┐                   │
              │  PrefSchedule   │◄──────────────────┤
              │   (Port 8080)   │                   │
              └────────┬────────┘                   │
                       │                            │
         ┌─────────────┼─────────────┐              │
         │             │             │              │
     ┌────▼─────┐  ┌───▼──────┐  ┌──▼─────────┐     │
     │QuickGrade│  │ Enricher │  │CourseEnrich│◄────┤
     │  :8081   │  │  :8082   │  │  :8083     │     │
     └──────────┘  └──────────┘  └────────────┘     │
                                                    │
                          ┌─────────────────────────┘
                          │
                    ┌─────▼──────┐
                    │  Grafana   │
                    │ Dashboards │
                    └────────────┘
```

### Message Processing Pipeline

```
┌─────────────┐  raw-grades-topic   ┌──────────┐  enriched-grades-topic
│ QuickGrade  │ ──────────────────> │ Enricher │ ──────────────────────>
│   (8081)    │   GradeEvent        │  (8082)  │   EnrichedGradeEvent
└─────────────┘                     └──────────┘
                                                                        
┌────────────────┐  grades_topic       ┌──────────────┐
│ CourseEnricher │ ──────────────────> │ PrefSchedule │
│    (8083)      │   FullGradeEvent    │   (8080)     │
└────────────────┘                     └──────────────┘
```

### Matching Orchestration Flow

```
┌──────────────┐                     ┌─────────────┐
│ PrefSchedule │ ──── REST/HTTP ───> │ StableMatch │
│              │ <─── Response ───── │  Instances  │
└──────┬───────┘                     └─────────────┘
       │
       │ Alternative: Event-Based (Async)
       ▼
┌──────────────────┐                ┌─────────────┐
│ matching-requests│ ──── Kafka ──> │ StableMatch │
│     Topic        │                │  Consumer   │
└──────────────────┘                └──────┬──────┘
                                           │
┌──────────────────┐                       │
│matching-responses│ <──── Kafka ──────────┘
│     Topic        │
└──────────────────┘
```

## 🔧 Microservices

### 1. **PrefSchedule** (Core Service)
**Port**: 8080  
**Database**: PostgreSQL  
**Responsibilities**:
- Student, instructor, and course management
- Student preference collection and validation
- Instructor preference configuration (weighted course importance)
- Grade storage and querying
- Matching orchestration and result persistence
- JWT-based authentication and authorization

**Key Endpoints**:
```
GET    /api/students
POST   /api/students
GET    /api/instructor-preferences
POST   /api/instructor-preferences
GET    /api/grades
POST   /api/grades/upload
POST   /api/matching/pack/{id}
GET    /actuator/health
```

### 2. **StableMatch** (Matching Engine)
**Ports**: 8084, 8086, 8087 (multi-instance)  
**Responsibilities**:
- Gale-Shapley stable matching algorithm implementation
- Random matching algorithm (fallback)
- Weighted student scoring based on instructor preferences
- Algorithm performance metrics collection
- Resilience pattern demonstrations

**Key Endpoints**:
```
POST   /api/matching/stable
POST   /api/matching/random
GET    /api/matching/assignments
GET    /api/matching/statistics
GET    /api/metrics/matching
```

**Algorithms**:
- **Stable Matching (Gale-Shapley)**: Ensures no student-course pair would prefer each other over their current assignments
- **Weighted Scoring**: Calculates student scores using instructor-defined compulsory course weights
- **Random Assignment**: Simple shuffle-based allocation for testing

### 3. **QuickGrade** (Grade Publisher)
**Port**: 8081  
**Responsibilities**:
- Publishing grade events to Kafka
- CSV batch upload processing
- Grade validation and formatting

**Message Format**:
```json
{
  "studentCode": "STU001",
  "courseCode": "COMP101",
  "grade": 9.5
}
```

### 4. **Enricher** (Student Data Enrichment)
**Port**: 8082  
**Responsibilities**:
- Consuming raw grade events
- Enriching with student name and year
- Publishing enriched events downstream

### 5. **CourseEnricher** (Course Data Enrichment)
**Port**: 8083  
**Responsibilities**:
- Consuming enriched grade events
- Adding course name and semester
- Publishing fully enriched events to PrefSchedule

### 6. **Eureka Server** (Service Discovery)
**Port**: 8761  
**Responsibilities**:
- Service registration and discovery
- Health monitoring
- Load balancing support

**Dashboard**: `http://localhost:8761`

### 7. **API Gateway**
**Port**: 8085  
**Responsibilities**:
- Unified entry point for all services
- Load balancing across StableMatch instances
- Circuit breaker integration
- CORS configuration
- Request routing

**Routing Examples**:
```
/api/matching/**        → lb://stablematch (round-robin)
/api/students/**        → lb://prefschedule
/api/grades/**          → lb://prefschedule
```

## 💻 Technologies

### Core Framework
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication & authorization
- **Spring Cloud** - Microservices infrastructure
- **Spring Kafka** - Event streaming

### Infrastructure
- **Apache Kafka 3.9.1** - Message broker
- **PostgreSQL** - Relational database
- **Eureka** - Service discovery
- **Spring Cloud Config** - Centralized configuration
- **HashiCorp Vault** - Secrets management

### Resilience & Fault Tolerance
- **Resilience4j** - Circuit breakers, bulkheads, rate limiters, retry
- **Spring Cloud LoadBalancer** - Client-side load balancing

### Monitoring & Observability
- **Micrometer** - Metrics instrumentation
- **Prometheus 2.48.0** - Metrics collection
- **Grafana 10.2.2** - Visualization & alerting
- **Spring Boot Actuator** - Health & metrics endpoints
- **SLF4J/Logback** - Logging

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **Spring Security Method Security** - Role-based access control

### Testing & Quality
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **Testcontainers** - Integration testing
- **JMeter 5.6.3** - Load testing
- **@WebMvcTest** - Controller testing

### Documentation & API
- **Springdoc OpenAPI 3** - API documentation
- **Swagger UI** - Interactive API explorer

### Build & Deployment
- **Maven** - Build tool
- **Docker** - Containerization
- **Kubernetes** - Orchestration (production)
- **Lombok** - Boilerplate reduction

## ✨ Features

### Domain Model

**Students**:
- Code, name, email, year
- User account with STUDENT role
- Course preferences (partial ordering per pack)

**Instructors**:
- Name, email
- User account with INSTRUCTOR role
- Course preferences (weighted compulsory course importance)

**Courses**:
- Type (compulsory/optional)
- Code, abbreviation, name
- Instructor assignment
- Pack membership (for optional courses)
- Group count, description

**Packs**:
- Year, semester, name
- Group optional courses for assignment

**Grades**:
- Student code, course code, grade
- Automatically stored for compulsory courses via Kafka pipeline

### Business Logic

#### 1. Student Preference Collection
- Students submit preferences for all optional courses in their year
- Preferences defined as partial ordering per pack
- Supports ties between courses
- Validated via Bean Validation

#### 2. Instructor Preference Configuration
- Instructors specify weighted importance of compulsory courses
- Example: `CO1: {(Math, 100%)}`, `CO2: {(OOP, 50%), (Java, 50%)}`
- Used to calculate weighted student scores for matching

#### 3. Grade Processing Pipeline
- **Stage 1**: QuickGrade publishes raw grade events
- **Stage 2**: Enricher adds student name and year
- **Stage 3**: CourseEnricher adds course name and semester
- **Stage 4**: PrefSchedule stores compulsory course grades
- Dead-Letter Queue (DLQ) for failed messages with 3 retries

#### 4. Stable Matching
- Gale-Shapley algorithm ensures stable assignments
- Students ranked by weighted grade averages
- Guarantees no blocking pairs exist
- Supports both synchronous (REST) and asynchronous (Kafka) invocation

### Resilience Patterns

| Pattern | Configuration | Purpose |
|---------|--------------|---------|
| **Retry** | 3 attempts, exponential backoff (1s, 2s, 4s) | Transient failure recovery |
| **Circuit Breaker** | 50% failure threshold, 5s recovery window | Prevent cascading failures |
| **Timeout** | 30 seconds | Avoid indefinite waits |
| **Bulkhead** | 5 concurrent calls | Resource isolation |
| **Rate Limiter** | 10 requests/second | Protect against overload |
| **Fallback** | Stable → Random → Empty | Graceful degradation |

### Security Features

- **JWT Authentication**: Stateless token-based auth with 24h expiration
- **Role-Based Access Control**: ADMIN, INSTRUCTOR, STUDENT roles
- **Password Security**: BCrypt hashing with salt
- **Method-Level Security**: `@PreAuthorize` annotations
- **Endpoint Protection**: POST/PUT/DELETE require authentication
- **Actuator Security**: Metrics require ADMIN role
- **Vault Integration**: Encrypted database passwords and secrets

### Monitoring Capabilities

#### Custom Metrics
- `stablematch.algorithm.invocations` - Counter by algorithm type
- `stablematch.algorithm.response.time` - Timer with percentiles
- JVM memory usage
- HTTP request rates
- Kafka consumer lag
- Circuit breaker state transitions

#### Grafana Dashboards
- Algorithm response time trends
- Invocation count visualization
- Memory usage gauges (color-coded thresholds)
- 95th percentile response time
- Request rate per service instance

#### Alerts
- **High Memory Usage**: >85% heap for 5 minutes
- **Slow Response Time**: p95 > 1000ms for 2 minutes
- Notification channels: console, email (configurable)

### API Features

- **Content Negotiation**: JSON and XML support
- **Conditional Requests**: ETag and If-None-Match headers
- **Error Handling**: Custom exceptions with global handler
- **Validation**: Bean Validation on DTOs
- **Pagination**: Spring Data pagination support
- **OpenAPI Documentation**: Swagger UI at `/swagger-ui.html`

## 📦 Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| Java JDK | 17+ | Runtime |
| Maven | 3.8+ | Build tool |
| PostgreSQL | 14+ | Database |
| Apache Kafka | 3.9.1 | Message broker |
| Prometheus | 2.48.0 | Metrics collection |
| Grafana | 10.2.2 | Visualization |
| HashiCorp Vault | 1.15.0 | Secrets management (optional) |
| Docker | 20+ | Containerization |
| Kubernetes | 1.25+ | Orchestration (production) |

### System Requirements

- **Memory**: 8GB RAM minimum (16GB recommended for full stack)
- **Disk**: 10GB free space
- **CPU**: 4 cores recommended

## 🚀 Installation & Setup

### 1. Clone Repository

```bash
git clone https://github.com/yourusername/prefschedule.git
cd prefschedule
```

### 2. Database Setup

```bash
# Start PostgreSQL
sudo service postgresql start

# Create database
psql -U postgres
CREATE DATABASE prefschedule;
\q
```

Run schema creation:
```bash
psql -U postgres -d prefschedule -f schema.sql
```

### 3. Kafka Setup

```bash
# Download Kafka 3.9.1
wget https://downloads.apache.org/kafka/3.9.1/kafka_2.13-3.9.1.tgz
tar -xzf kafka_2.13-3.9.1.tgz
cd kafka_2.13-3.9.1

# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties &

# Start Kafka broker
bin/kafka-server-start.sh config/server.properties &

# Create topics
bin/kafka-topics.sh --create --topic raw-grades-topic --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic enriched-grades-topic --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic grades_topic --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic grades_topic.DLT --partitions 1 --replication-factor 1 --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic matching-requests --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic matching-responses --partitions 3 --replication-factor 1 --bootstrap-server localhost:9092
```

### 4. Prometheus Setup

```bash
# Download and extract
wget https://github.com/prometheus/prometheus/releases/download/v2.48.0/prometheus-2.48.0.linux-amd64.tar.gz
tar -xzf prometheus-2.48.0.linux-amd64.tar.gz
cd prometheus-2.48.0.linux-amd64

# Configure (use provided prometheus.yml)
cp ~/prefschedule/config/prometheus.yml .

# Start Prometheus
./prometheus --config.file=prometheus.yml &
```

Access: `http://localhost:9090`

### 5. Grafana Setup

```bash
# Download and install
wget https://dl.grafana.com/oss/release/grafana-10.2.2.linux-amd64.tar.gz
tar -xzf grafana-10.2.2.linux-amd64.tar.gz
cd grafana-10.2.2

# Start Grafana
./bin/grafana-server &
```

Access: `http://localhost:3000` (admin/admin)

**Configure Data Source**:
1. Add Prometheus data source: `http://localhost:9090`
2. Import dashboard from `config/grafana-dashboard.json`

### 6. HashiCorp Vault Setup (Optional)

```bash
# Download and install
wget https://releases.hashicorp.com/vault/1.15.0/vault_1.15.0_linux_amd64.zip
unzip vault_1.15.0_linux_amd64.zip
sudo mv vault /usr/local/bin/

# Start in dev mode
vault server -dev &

# Set token
export VAULT_TOKEN=<root-token-from-output>
export VAULT_ADDR='http://localhost:8200'

# Store secrets
vault kv put secret/stablematch database.username=stablematch_user database.password=secretpass
vault kv put secret/prefschedule database.username=prefschedule_user database.password=secretpass
```

### 7. Build All Services

```bash
# Build parent project
mvn clean install

# Or build individually
cd eureka-server && mvn clean package
cd ../config-server && mvn clean package
cd ../api-gateway && mvn clean package
cd ../prefschedule && mvn clean package
cd ../stablematch && mvn clean package
cd ../quickgrade && mvn clean package
cd ../enricher && mvn clean package
cd ../course-enricher && mvn clean package
```

### 8. Start Services (Development)

**Recommended Order**:

```bash
# 1. Infrastructure services
cd eureka-server && mvn spring-boot:run &
cd ../config-server && mvn spring-boot:run &

# Wait 30 seconds for Eureka and Config Server to start

# 2. Processing pipeline
cd ../enricher && mvn spring-boot:run &
cd ../course-enricher && mvn spring-boot:run &
cd ../quickgrade && mvn spring-boot:run &

# 3. StableMatch instances
cd ../stablematch && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8084 &
cd ../stablematch && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8086 &
cd ../stablematch && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8087 &

# 4. Core service
cd ../prefschedule && mvn spring-boot:run &

# 5. API Gateway
cd ../api-gateway && mvn spring-boot:run &
```

**Using Helper Scripts**:

```bash
# Linux/Mac
chmod +x scripts/*.sh
./scripts/start-all.sh

# Windows
scripts\start-all.bat
```

### 9. Verify Deployment

```bash
# Check Eureka dashboard
curl http://localhost:8761

# Check all services registered
curl http://localhost:8761/eureka/apps | grep -i "<app>"

# Test API Gateway
curl http://localhost:8085/api/students

# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Test matching endpoint
curl -X POST http://localhost:8085/api/matching/pack/1 \
  -H "Authorization: Bearer <your-jwt-token>"
```

## ⚙️ Configuration

### Application Configuration

Services use Spring Cloud Config Server pulling from Git repository:

```yaml
# config-repo/application.yml (shared)
spring:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
  
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
```

### Service-Specific Configuration

**PrefSchedule** (`config-repo/prefschedule.yml`):
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/prefschedule
    username: ${database.username}  # From Vault
    password: ${database.password}  # From Vault
  
  kafka:
    consumer:
      group-id: prefschedule-group
      bootstrap-servers: localhost:9092

jwt:
  secret: ${jwt.secret}  # From Vault
  expiration: 86400000

matching:
  default-algorithm: stable
```

**StableMatch** (`config-repo/stablematch.yml`):
```yaml
server:
  port: 8084

matching:
  algorithm:
    default: stable
    timeout-seconds: 30
  cache:
    enabled: true
    ttl-minutes: 10
```

### Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    instances:
      stableMatchService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        sliding-window-size: 10
  
  retry:
    instances:
      stableMatchService:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
  
  bulkhead:
    instances:
      stableMatchService:
        max-concurrent-calls: 5
  
  ratelimiter:
    instances:
      stableMatchService:
        limit-for-period: 10
        limit-refresh-period: 1s
```

### Environment Variables

```bash
# Database
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=prefschedule

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Vault
export VAULT_TOKEN=<your-token>
export VAULT_ADDR=http://localhost:8200

# JWT
export JWT_SECRET=<your-secret>  # Or use Vault
```

## 📚 API Documentation

### Authentication

**Register User**:
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "student1",
  "password": "securepass",
  "email": "student1@university.edu",
  "role": "STUDENT"
}
```

**Login**:
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "student1",
  "password": "securepass"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000
}
```

### Student Management

**Get All Students**:
```bash
GET /api/students
Authorization: Bearer <token>

Response:
[
  {
    "id": 1,
    "code": "STU001",
    "name": "John Doe",
    "email": "john@university.edu",
    "year": 3
  }
]
```

**Create Student**:
```bash
POST /api/students
Authorization: Bearer <token>
Content-Type: application/json

{
  "code": "STU002",
  "name": "Jane Smith",
  "email": "jane@university.edu",
  "year": 2
}
```

### Preference Management

**Submit Student Preferences**:
```bash
POST /api/preferences/student
Authorization: Bearer <token>
Content-Type: application/json

{
  "studentCode": "STU001",
  "packId": 1,
  "preferences": [
    {"courseCode": "OPT101", "rank": 1},
    {"courseCode": "OPT102", "rank": 2},
    {"courseCode": "OPT103", "rank": 2}  // Tie
  ]
}
```

**Configure Instructor Preferences**:
```bash
POST /api/instructor-preferences
Authorization: Bearer <token>
Content-Type: application/json

{
  "courseCode": "OPT101",
  "instructorId": 5,
  "weights": [
    {"compulsoryCourseAbbr": "Math", "percentage": 100},
    {"compulsoryCourseAbbr": "OOP", "percentage": 50}
  ]
}
```

### Grade Management

**Upload Grades (CSV)**:
```bash
POST /api/grades/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

--form 'file=@grades.csv'

CSV Format:
STU001,COMP101,9.5
STU002,COMP102,8.7
```

**Get Student Grades**:
```bash
GET /api/grades?studentCode=STU001
Authorization: Bearer <token>
```

### Matching Endpoints

**Trigger Synchronous Matching**:
```bash
POST /api/matching/pack/1
Authorization: Bearer <token>

Response:
{
  "packId": 1,
  "algorithm": "stable",
  "assignments": [
    {"studentCode": "STU001", "courseCode": "OPT101"},
    {"studentCode": "STU002", "courseCode": "OPT102"}
  ],
  "executionTimeMs": 145
}
```

**Trigger Asynchronous Matching**:
```bash
POST /api/matching/async/pack/1
Authorization: Bearer <token>

Response:
{
  "requestId": "abc-123-def",
  "status": "PROCESSING",
  "message": "Matching request queued"
}
```

**Get Matching Statistics**:
```bash
GET /api/matching/statistics
Authorization: Bearer <token>

Response:
{
  "totalAssignments": 150,
  "stableMatchCount": 148,
  "randomMatchCount": 2,
  "averageResponseTimeMs": 142.5
}
```

### Swagger UI

Interactive API documentation available at:
```
http://localhost:8085/swagger-ui.html
```

## 📊 Monitoring & Observability

### Health Checks

```bash
# Individual service health
curl http://localhost:8080/actuator/health

# Gateway health (aggregated)
curl http://localhost:8085/actuator/health
```

### Prometheus Queries

**Algorithm Response Time**:
```promql
rate(stablematch_algorithm_response_time_seconds_sum{algorithm="stable"}[1m]) 
/ 
rate(stablematch_algorithm_response_time_seconds_count{algorithm="stable"}[1m]) 
* 1000
```

**Memory Usage**:
```promql
100 * (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"})
```

**Request Rate**:
```promql
rate(http_server_requests_seconds_count{uri=~"/api/matching.*"}[1m])
```

### Grafana Dashboards

**Access**: `http://localhost:3000`

**Included Panels**:
- Algorithm response time (time series)
- Invocation counters (bar chart)
- Memory usage (gauge)
- 95th percentile latency (stat)
- Request rate per instance (graph)
- Circuit breaker states (state timeline)

### Resilience Pattern Monitoring

```bash
# Circuit breaker state
curl http://localhost:8080/api/resilience/monitor/circuit-breaker/stableMatchService

# Rate limiter metrics
curl http://localhost:8080/api/resilience/monitor/rate-limiter/stableMatchService

# All patterns overview
curl http://localhost:8080/api/resilience/monitor/all
```

### Kafka Monitoring

```bash
# Consumer lag
kafka-consumer-groups.sh --describe \
  --group prefschedule-group \
  --bootstrap-server localhost:9092

# Topic metrics
kafka-topics.sh --describe \
  --topic grades_topic \
  --bootstrap-server localhost:9092
```

## 🔒 Security

### Authentication Flow

1. User registers via `/api/auth/register`
2. Password hashed with BCrypt (strength 10)
3. User logs in via `/api/auth/login`
4. Server issues JWT with 24h expiration
5. Client includes token in `Authorization: Bearer <token>` header
6. Server validates signature and expiration on each request

### Authorization Rules

| Endpoint | Role Required |
|----------|--------------|
| `GET /api/students` | PUBLIC |
| `POST /api/students` | ADMIN |
| `POST /api/preferences/student` | STUDENT (own data) |
| `POST /api/instructor-preferences` | INSTRUCTOR (own courses) |
| `POST /api/matching/**` | ADMIN |
| `GET /actuator/metrics` | ADMIN |
| `GET /actuator/health` | PUBLIC |

### Method-Level Security Example

```java
@PreAuthorize("hasRole('ADMIN') or #studentCode == authentication.principal.username")
public List<Grade> getStudentGrades(String studentCode) {
    // ...
}
```

### Secrets Management

All sensitive credentials stored in HashiCorp Vault:
- Database passwords
- JWT signing keys
- Kafka credentials
- SMTP passwords

**Accessing Secrets**:
```java
@Value("${database.password}")
private String dbPassword;  // Injected from Vault
```

## 🧪 Testing

### Unit Tests

```bash
# Run all tests
mvn test

# Run specific service tests
cd prefschedule
```

```

**Example Test**:
```java
@WebMvcTest(StudentController.class)
class StudentControllerTest {
    
    @MockBean
    private StudentService studentService;
    
    @Test
    void shouldReturnStudentList() throws Exception {
        when(studentService.findAll()).thenReturn(List.of(student1, student2));
        
        mockMvc.perform(get("/api/students"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }
}
```

### Integration Tests (Testcontainers)

```bash
mvn verify
```

**Example**:
```java
@Testcontainers
@SpringBootTest
class StudentRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");
    
    @Test
    void shouldSaveStudent() {
        Student student = new Student("STU001", "John Doe", "john@edu.edu", 3);
        repository.save(student);
        
        assertThat(repository.findByCode("STU001")).isPresent();
    }
}
```

### Load Testing (JMeter)

```bash
# Run all stress tests
jmeter -n -t tests/JMeter_StressTests/all-patterns.jmx -l results.jtl

# View results
jmeter -g results.jtl -o reports/
```

**Test Scenarios**:
1. **Rate Limiter Test**: 20 concurrent requests in 1 second
2. **Circuit Breaker Test**: 10 failing requests to trigger opening
3. **Bulkhead Test**: 10 concurrent requests with 5s delays
4. **Timeout Test**: 35s requests exceeding 30s limit
5. **Combined Stress Test**: 50 threads for 60 seconds

### Manual Testing

**Test Matching Flow**:
```bash
# 1. Populate data
curl -X POST http://localhost:8080/api/students/populate

# 2. Upload grades
curl -X POST http://localhost:8080/api/grades/upload -F "file=@test-grades.csv"

# 3. Configure instructor preferences
curl -X POST http://localhost:8080/api/instructor-preferences \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"courseCode":"OPT101","weights":[{"compulsoryCourseAbbr":"Math","percentage":100}]}'

# 4. Trigger matching
curl -X POST http://localhost:8085/api/matching/pack/1 \
  -H "Authorization: Bearer $TOKEN"

# 5. Verify assignments
curl http://localhost:8085/api/matching/assignments | jq
```

**Test Resilience Patterns**:
```bash
# Enable failure simulation
curl -X POST http://localhost:8084/api/test/enable-failures

# Trigger circuit breaker
for i in {1..10}; do
  curl http://localhost:8085/api/matching/pack/1
done

# Check circuit breaker opened
curl http://localhost:8080/api/resilience/monitor/circuit-breaker/stableMatchService
```

## 🐳 Deployment

### Docker Build

**Build Images**:
```bash
# Build all services
docker-compose build

# Or build individually
cd prefschedule && docker build -t prefschedule:latest .
```

**Docker Compose** (`docker-compose.yml`):
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: prefschedule
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
  
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    ports:
      - "9092:9092"
  
  eureka:
    image: prefschedule/eureka-server:latest
    ports:
      - "8761:8761"
  
  config-server:
    image: prefschedule/config-server:latest
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
    ports:
      - "8888:8888"
  
  stablematch:
    image: prefschedule/stablematch:latest
    environment:
      EUREKA_CLIENT_SERVICEURL_DEFAULTZONE: http://eureka:8761/eureka/
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
    ports:
      - "8084:8084"
    deploy:
      replicas: 3
  
  prefschedule:
    image: prefschedule/prefschedule:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/prefschedule
      KAFKA_BOOTSTRAP_SERVERS: kafka:9092
    ports:
      - "8080:8080"
  
  api-gateway:
    image: prefschedule/api-gateway:latest
    ports:
      - "8085:8085"
```

**Run Stack**:
```bash
docker-compose up -d
```

### Kubernetes Deployment

**Deploy Infrastructure**:
```bash
# Create namespace
kubectl create namespace prefschedule

# Deploy PostgreSQL
kubectl apply -f k8s/postgres-deployment.yaml

# Deploy Kafka
kubectl apply -f k8s/kafka-deployment.yaml

# Deploy Eureka
kubectl apply -f k8s/eureka-deployment.yaml
```

**Deploy Services**:
```bash
kubectl apply -f k8s/prefschedule-deployment.yaml
kubectl apply -f k8s/stablematch-deployment.yaml
kubectl apply -f k8s/api-gateway-deployment.yaml
```

**Example Deployment** (`k8s/stablematch-deployment.yaml`):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: stablematch
  namespace: prefschedule
spec:
  replicas: 3
  selector:
    matchLabels:
      app: stablematch
  template:
    metadata:
      labels:
        app: stablematch
    spec:
      containers:
      - name: stablematch
        image: prefschedule/stablematch:1.0.0
        ports:
        - containerPort: 8084
        env:
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: "http://eureka:8761/eureka/"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8084
          initialDelaySeconds: 60
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8084
---
apiVersion: v1
kind: Service
metadata:
  name: stablematch
  namespace: prefschedule
spec:
  selector:
    app: stablematch
  ports:
  - port: 8084
    targetPort: 8084
  type: ClusterIP
```

**Scaling**:
```bash
# Scale StableMatch instances
kubectl scale deployment stablematch --replicas=5 -n prefschedule

# Autoscaling
kubectl autoscale deployment stablematch \
  --cpu-percent=70 \
  --min=3 \
  --max=10 \
  -n prefschedule
```

### CI/CD Pipeline

**GitHub Actions** (`.github/workflows/deploy.yml`):
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: mvn clean verify
  
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - run: docker build -t prefschedule/stablematch:${{ github.sha }} .
      - run: docker push prefschedule/stablematch:${{ github.sha }}
  
  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - run: kubectl set image deployment/stablematch stablematch=prefschedule/stablematch:${{ github.sha }} -n prefschedule
```

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit changes**: `git commit -m 'Add amazing feature'`
4. **Push to branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Code Standards

- Follow Spring Boot best practices
- Write unit tests for new features (>80% coverage)
- Update documentation for API changes
- Use conventional commits: `feat:`, `fix:`, `docs:`, `refactor:`