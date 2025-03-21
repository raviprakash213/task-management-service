# Task Management Service

## Overview
The **Task Management Service** is a Spring Boot-based application that provides Asynchronous Data Processing Pipeline for managing tasks asynchronously using Kafka.

## Features
- **Spring Boot 3.x** for rapid development.
- **Spring Data JPA** with PostgreSQL for database persistence.
- **H2 Database** for testing.
- **Kafka** as the message broker for asynchronous processing.
- **Retryable Kafka Topics** using `@RetryableTopic` for automatic retries.
- **Caffeine Caching** for improved performance.
- **Spring Boot Actuator** for health checks and monitoring.
- **Global Exception Handling** for consistent API responses.
- **Logging** for debugging and tracing.
- **JUnit & MockMvc** for unit and component testing.
- **Prometheus & Grafana** for Monitoring and Reporting.
- **MapStruct** for efficient DTO mapping.
- **Resilience4j** for retry and fallback mechanisms in asynchronous task processing.
- **Swagger** for API documentation.
- **Sorting & Pagination** support for listing tasks efficiently.

## Technologies Used
- Java 17
- Spring Boot 3.x
- Spring Data JPA & Hibernate
- PostgreSQL & H2 Database
- Kafka
- Spring Kafka (`@RetryableTopic` for retries)
- Caffeine Cache (As application runing on local) for distributed environment can be replaced with Redis
- Logback
- Spring Boot Actuator
- Prometheus & Grafana
- JUnit/Mockito & MockMvc
- MapStruct
- Resilience4j (Retry & Fallback)
- Swagger (OpenAPI 3)

## Prerequisites
- Java 17
- Maven
- Kafka installed at `C:\kafka`
- PostgreSQL database
- Prometheus and Grafana installed in local

## Setup Instructions

### 1. Clone the Repository

### 2. Install Postgresql then create a database and table called taskmanagement(SQL script provided) 

### 3. Start Kafka (Windows)
```sh
cd C:\kafka
.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties - Start Zookeeper
.\bin\windows\kafka-server-start.bat .\config\server.properties        - Start Kafka Server
.\bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --topic {topic_Name} - Create a topic 
```

### 4. Configure the Application
Modify `application.yml` to set database credentials and the kafka Topic Name you created, or resue the database credentials and topicName I used in project

### 5 Install Prometheus 
1)Download Prometheus 

2)Extract the files and move them to C:\prometheus.

3) Update prometheus.yml with the following content:
  global:
    scrape_interval: 15s
  
  scrape_configs:
    - job_name: 'spring-boot-app'
      metrics_path: '/actuator/prometheus'
      static_configs:
        - targets: ['localhost:8080'] # Replace with your Spring Boot app's host and port
        
Then  Navigate to the Prometheus directory and start the server:

    cd C:\prometheus
    .\prometheus.exe --config.file=prometheus.yml

4. Access Prometheus at http://localhost:9090

### 6 Install Grafana 
1.Download Grafana

2.Install Grafana and start the service.

3 go to http://localhost:3000.

4.Log in with the default credentials (admin/admin).

5.Navigate to Configuration > Data Sources.

6.Click Add data source and select Prometheus.

7.Set the URL to http://localhost:9090.

8.Click Save & Test.

9.Add metrics(query)  for tasks_submitted_total, tasks_failed_total,tasks_completed_total counters as created in the application


### 7. Build and Run the Application
```sh
mvn clean install
mvn spring-boot:run
```

### 8. Running Tests
```sh
mvn test
```

## API Endpoints


### Task Management
| Method | Endpoint                 | Description                   |
|--------|--------------------------|-------------------------------|
| GET    | `/taskManagement`        | Get all tasks                 |
| POST   | `/taskManagement`        | Submit a new task             |
| GET    |`/taskManagement/status/{id}`| Get status of a task by ID |
| GET    | `/taskManagement/statistics`            | Retrieves aggregated task statistics          |


### Swagger API Documentation
Swagger UI is available at:
```
http://localhost:8080/swagger-ui/index.html
```


