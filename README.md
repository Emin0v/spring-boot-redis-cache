# Spring Boot & Redis Cache Service

This project is a Spring Boot application that demonstrates a robust caching strategy using Redis to optimize data retrieval for product and service codes.

## Overview

The service manages `ProductServiceCode` entities, providing RESTful endpoints to create, update, and retrieve these codes. It leverages a read-through/write-through caching mechanism with Redis to minimize database load and improve response times for frequent read operations.

## Prerequisites

- Java 21
- Docker and Docker Compose
- Gradle 8.8

## Getting Started

### 1. Run the Infrastructure

The required infrastructure (PostgreSQL and Redis) is managed via Docker Compose.

```bash
docker-compose -f infra/docker-compose.yaml up -d
```

This command will start a PostgreSQL database and a Redis instance in the background.

### 2. Run the Application

You can run the Spring Boot application using the Gradle wrapper:

```bash
./gradlew bootRun
```

The application will connect to the services started in the previous step. The API will be available at `http://localhost:8085`.

## API Endpoints

The following endpoints are available under the base path `/api/v1/codes`:

| Method | Endpoint             | Description                                         |
|--------|----------------------|-----------------------------------------------------|
| `GET`    | `/`                  | Retrieves all product and service codes.            |
| `GET`    | `/by-type?type=...`  | Retrieves all codes of a specific type (`PRODUCT` or `SERVICE`). |
| `POST`   | `/`                  | Creates a new product or service code.              |
| `PUT`    | `/{id}`              | Updates an existing product or service code.        |

---

## Caching Strategy

The application implements a sophisticated read-through/write-through caching model to ensure high performance and data consistency.

### Cache Logic Flow

1.  **Read Operations (`findAll`, `findByType`):**
    - The application first requests data from the Redis cache.
    - **Cache Hit:** If the data exists in Redis, it is returned directly to the client.
    - **Cache Miss:** If the data is not in Redis, the application queries the PostgreSQL database. The retrieved data is then saved to the Redis cache for future requests before being returned to the client.

2.  **Write Operations (`createCode`, `updateCode`):**
    - The application writes the data to the PostgreSQL database first.
    - Upon a successful database write, the application immediately writes the same data to the Redis cache. This ensures that the cache remains synchronized with the database.

### Redis Data Model

To support efficient lookups, the cache is structured using multiple Redis data types:

-   **`HASH` for Core Data:**
    -   **Key:** `dict:codes`
    -   **Description:** A single Redis Hash stores all `ProductServiceCode` objects. The field within the hash is the code's `id`, and the value is the JSON-serialized object. This allows for O(1) lookup of any code by its ID.

-   **`SETs` for Indexing:**
    -   **Key:** `dict:index:all`
    -   **Description:** This Set stores the IDs of every code in the system. It is used to efficiently fetch all codes without needing to scan the main hash.
    -   ---
    -   **Key:** `dict:index:category:<TYPE>` (e.g., `dict:index:category:PRODUCT`)
    -   **Description:** A separate Set is maintained for each `ClassificationType`. Each set stores the IDs of all codes belonging to that category. This allows for fast retrieval of all codes of a specific type.

### Time-to-Live (TTL)

- All cache keys are configured with a **30-day TTL**. This policy ensures that if the data is not accessed or updated for 30 days, it is automatically evicted from the cache, freeing up memory and preventing excessively stale data.

---

## Database Migrations

Database schema changes are managed by [Liquibase](https://www.liquibase.org/). Migration scripts are located in `src/main/resources/liquibase/changelog/`.

- To add a new migration, create a new YAML file in the changelog directory and include it in `changelog-master.yaml`.

## Running Tests

To run the test suite, use the following Gradle command:

```bash
./gradlew test
```
