# Transaction API
[![Java CI with Gradle](https://github.com/Lucasgm22/Transaction/actions/workflows/build-and-publish.yaml/badge.svg)](https://github.com/Lucasgm22/Transaction/actions/workflows/build-and-publish.yaml)

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
* **Caching:** Caffeine
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
  - `cache_...`: Cache sizes, hits and misses.
  - JVM performance metrics (memory, CPU, garbage collection).

---

## ⚡ Caching Strategy & Performance

To optimize performance and increase resilience against external service failures, a sophisticated caching layer was implemented for the Treasury API client calls.

### Implementation

The chosen strategy goes beyond simple caching and uses a **proactive cache warming** technique, implemented programmatically within the `ExchangeRateService`.

1.  **Cache Provider:** The high-performance in-memory library **Caffeine** was chosen as the cache provider to enable advanced features like TTL and size-based eviction.
2.  **Proactive Warming Logic:** Instead of just caching the result for a single requested date, the service analyzes the response from the Treasury API. After a single successful API call for a given date, the cache is pre-populated with the same result for all subsequent dates that would share the same exchange rate (based on the 6-month lookup rule). This is designed to maximize the cache hit ratio for date-based lookups.
3.  **Observability:** The Caffeine cache is fully instrumented using Micrometer. Detailed performance metrics, including hit/miss ratios, size, and evictions, are exposed via the `/actuator/prometheus` endpoint.

### Performance Impact

The effectiveness of this strategy was validated through a mixed-workload stress test (using k6). The following metrics were collected from Prometheus after the test, which generated nearly **700,000 requests**.

* **Cache Hit Ratio:** **99.6%**
    * `cache_gets_total{result="hit"}`: 563,135
    * `cache_gets_total{result="miss"}`: 2,032

* **External API Calls Avoided:**
    * To serve over **565,000** cache lookups, the application only needed to make **97** actual calls to the external Treasury API.
    * The cache handled over **99.6%** of the load, proving a massive reduction in external network dependency.

* **Latency Reduction:**
    * The average latency of a real call to the external API was measured at **~515ms**.
    * Thanks to the cache, the application's overall p95 latency during the stress test was only **~58ms**.

**Conclusion:** The data proves that the proactive cache warming strategy is extremely effective. It dramatically reduces dependency on the external service, improves the application's resilience, and ensures a highly responsive experience by serving the vast majority of requests from the ultra-fast in-memory cache.

---


## ⏱️ Performance Testing

Performance tests were conducted to validate the application's behavior under different types of load.

### Test Environment

The entire test environment, including the application, and load generator, was run using Docker to ensure consistency and reproducibility.

* **Host Machine:** Intel i5-1035G1 (4 Cores, 8 Threads), 16GB RAM
* **Container Resource Allocation:**
  * **Application (This Project):**
    * CPU Limit: `1.5 vCPUs`
    * Memory Limit: `2 GB`
  * **Load Generator (k6):**
    * CPU Limit: `2.0 vCPUs`
    * Memory Limit: `1 GB`

### Scenario 1: Realistic Load Test

This test simulates a realistic workload of 100 concurrent users with a 1-second pacing time between requests, representing users interacting with the system in a normal fashion.

| Metric | Value          |
| :--- |:---------------|
| **Requests per Second (RPS)** | **~82 reqs/s** |
| **p95 Latency** | **5.76 ms**    |
| **Error Rate** | **0.00%**      |

**Analysis:** Under a realistic mixed load, the application is extremely responsive and stable.

### Scenario 2: Stress Test (Maximum Throughput)

This test was conducted without any pacing time (`sleep`) to determine the maximum throughput of the application with 100 concurrent threads.

| Metric | Value              |
| :--- |:-------------------|
| **Requests per Second (RPS)** | **~1, 560 reqs/s** |
| **p95 Latency** | **272.14ms**       |
| **Error Rate** | **0.00%**          |

**Analysis:** The stress test revealed a maximum throughput of approximately 1,560 requests per second. Even at this peak load, the application remained perfectly stable with a 0% error rate, and the p95 latency was excellent at 272.14ms, demonstrating a highly efficient and robust architecture.
