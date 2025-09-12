# Transaction API
[![Java CI with Gradle](https://github.com/Lucasgm22/Transaction/actions/workflows/build.yaml/badge.svg)](https://github.com/Lucasgm22/Transaction/actions/workflows/build.yaml)

This project is a solution for a technical challenge that involves creating a RESTful API to store purchase transactions and retrieve them with their value converted to a specified country's currency. The application is built with a focus on modern, production-ready practices, including comprehensive testing, documentation, and a full suite of observability features.

---

## ✨ Features

* **Store Transactions:** Persist purchase transactions with a description, date, and amount in USD.
* **Currency Conversion:** Retrieve any stored transaction with its purchase amount converted to a supported currency using exchange rates from the [U.S. Treasury Reporting Rates of Exchange API](https://fiscaldata.treasury.gov/datasets/treasury-reporting-rates-exchange/treasury-reporting-rates-of-exchange).
* **Robust Validation:** Strong server-side validation for all incoming data and API parameters.
* **Professional Error Handling:** A global exception handler provides consistent and informative error responses.
* **Full Observability Suite:**
  * **Metrics:** Exposes detailed application and business metrics in Prometheus format.
  * **Distributed Tracing:** Instruments all requests to enable performance analysis and debugging.
  * **Health Checks:** Provides Liveness and Readiness probes for container orchestration platforms like Kubernetes.
* **Interactive API Documentation:** Auto-generated, interactive API documentation via Springdoc OpenAPI (Swagger UI).
* **Container-Ready:** Natively supports being packaged as an optimized Docker container image.

---

## 🛠️ Tech Stack

* **Language & Framework:** Java 21+, Spring Boot 3+
* **Data:** Spring Data JPA, H2 Database (In-Memory)
* **API Client:** Spring `RestClient`
* **Build Tool:** Gradle
* **Testing:** JUnit 5, Mockito, WireMock
* **Documentation:** Springdoc OpenAPI
* **Observability:** Spring Boot Actuator, Micrometer (for Metrics & Tracing), Prometheus registry

---

## 🚀 Getting Started

### Prerequisites

* Java (JDK) 21 or newer
* Gradle 8.x

### Running the Application

1.  Clone the repository:
    ```bash
    git clone https://github.com/Lucasgm22/Transaction.git
    ```

2.  Run the application using the Gradle wrapper:
    ```bash
    ./gradlew bootRun
    ```

The application will start on `http://localhost:8080`.

### Running Tests

Execute the full suite of unit and integration tests:

```bash
./gradlew test
```

A test report will be generated in build/reports/tests/test/index.html.

---

## 🐳 Docker Support

This application can be easily containerized into an optimized Docker image without needing a Dockerfile, thanks to Spring Boot's native support for Cloud Native Buildpacks.

1. **Build the Image:**
Execute the `bootBuildImage` Gradle task. This will create a Docker image with a name based on the project's artifact ID and version (e.g., `transaction:0.0.1-SNAPSHOT`).
```bash
./gradlew bootBuildImage
```

2. **Run the Container:**
Once the image is built, run it using Docker:
```bash
docker run --rm -p 8080:8080 -p 4444:4444 <your-image-name>:<version>

# Example:
# docker run --rm -p 8080:8080 transaction:0.0.1-SNAPSHOT
```

---

## 📄 API Documentation (Swagger)
Interactive API documentation is automatically generated and available once the application is running.

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **OpenAPI 3.0 Spec:** http://localhost:8080/v3/api-docs

The UI allows you to explore all endpoints, view their models, and execute API calls directly from your browser.

---

## 🔬 Observability

The application is fully instrumented for production-grade observability, accessible via port `4444`.

**Health Checks (Liveness & Readiness Probes)**

The application exposes Kubernetes-compatible health probes to ensure reliable operation in a containerized environment.

- **Liveness Probe:** `GET /actuator/health/liveness`
    - Indicates if the application is running. A failure will cause the container to be restarted.
- **Readiness Probe:** `GET /actuator/health/readiness`
    - Indicates if the application is ready to accept traffic. It checks its internal state and its connection to critical dependencies (Database, Treasury API). A failure will cause the container to be temporarily removed from the load balancer.

**Metrics (Prometheus)**

Application, JVM, and custom business metrics are exposed in Prometheus format.
- **Prometheus Endpoint:** `GET /actuator/prometheus`
- **Key Metrics:**
  - `http_server_requests_seconds`: Latency and count for all incoming API requests.
  - `http_client_requests_seconds`: Latency and count for outgoing calls made by the `RestClient`.
  - JVM performance metrics (memory, CPU, garbage collection).

---

## 🏗️ Architectural Decisions
A few key architectural decisions were made during development:

- **Observability-First:** Observability features (metrics, traces, health checks) were integrated from the start, not as an afterthought.

- **Client Abstraction:** Communication with the external Treasury API is encapsulated in a dedicated `TreasuryApiClient`, separating integration logic from business logic. This improves testability and maintainability.

- **Robust Validation:** A "fail-fast" approach was taken, with a flexible "sanity check" validation layer at the API boundary to reject malformed requests early, while delegating the final source-of-truth validation to the Treasury API itself.

- **Configuration Management:** Key configuration details like the external API's base URL are managed in `application.yaml` to allow for easy changes across different environments without modifying the code.

---

## ⏱️ Performance Testing

Performance tests were conducted to validate the application's behavior under different types of load.

### Scenario 1: Realistic Load Test

This test simulates a realistic workload of 100 concurrent users with a 1-second pacing time between requests, representing users interacting with the system in a normal fashion.

| Metric | Value |
| :--- | :--- |
| **Requests per Second (RPS)** | **~82 reqs/s** |
| **p95 Latency** | **5.26 ms** |
| **Error Rate** | **0.00%** |

**Analysis:** Under a realistic mixed load, the application is extremely responsive and stable.

### Scenario 2: Stress Test (Maximum Throughput)

This test was conducted without any pacing time (`sleep`) to determine the maximum throughput of the application with 100 concurrent threads.

| Metric | Value |
| :--- | :--- |
| **Requests per Second (RPS)** | **~4,823 reqs/s** |
| **p95 Latency** | **36.09 ms** |
| **Error Rate** | **0.00%** |

**Analysis:** The stress test revealed a maximum throughput of approximately 4,800 requests per second. Even at this peak load, the application remained perfectly stable with a 0% error rate, and the p95 latency was excellent at ~36ms, demonstrating a highly efficient and robust architecture.

---
