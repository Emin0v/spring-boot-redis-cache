# Spring Boot & Redis Cache Service

This Spring Boot example explores application-managed Redis caching for product and service codes.

## Overview

The service manages `ProductServiceCode` entities through REST endpoints. It uses cache-aside
reads and invalidates cached catalogue/category snapshots after database writes. PostgreSQL
remains the source of truth.

## Prerequisites

- Java 21
- Docker and Docker Compose
- The included Gradle wrapper

## Getting Started

### 1. Run the Infrastructure

The required infrastructure (PostgreSQL and Redis) is managed via Docker Compose.

```bash
docker compose -f infra/docker-compose.yaml up -d
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

The application uses Redis hashes and sets to cache complete results for a queried scope.
Full-catalogue and category reads populate their own indexes; a category result cannot mark
the entire catalogue as cached.

### Cache Logic Flow

1.  **Read Operations (`findAll`, `findByType`):**
    - The application first requests data from the Redis cache.
    - **Cache Hit:** Return the result only when every indexed ID has a corresponding cached object.
    - **Cache Miss:** Query PostgreSQL for the requested scope and populate that scope's cache.
      Replacing a full-catalogue snapshot also replaces its category indexes.

2.  **Write Operations (`createCode`, `updateCode`):**
    - The application writes the data to the PostgreSQL database first.
    - After the database write, invalidate the shared hash and all catalogue/category indexes.
      The next read reloads its scope from PostgreSQL. This avoids treating one written row as
      a complete catalogue and removes old membership after classification changes.

### Consistency boundaries

- Database writes and Redis invalidation are separate operations. Redis failure can follow a
  committed database write, leaving stale cache data and an error response.
- A read already in flight can repopulate an older snapshot after invalidation. This example
  does not serialize database reads with cache writes or promise linearizable consistency.
- Direct database changes bypass application invalidation. Empty results are not cached.
- Invalidating the whole small catalogue favors simpler correctness over fine-grained cache
  maintenance. Higher write rates would require measuring that cost and choosing a stronger
  invalidation/versioning policy where needed.

### Redis Data Model

To support efficient lookups, the cache is structured using multiple Redis data types:

-   **`HASH` for Core Data:**
    -   **Key:** `dict:codes`
    -   **Description:** A single Redis Hash stores all `ProductServiceCode` objects. The field within the hash is the code's `id`, and the value is the JSON-serialized object. This allows for O(1) lookup of any code by its ID.

-   **`SETs` for Indexing:**
    -   **Key:** `dict:index:all`
    -   **Description:** IDs from a full-catalogue load. Category-only loads do not populate this key.
    -   ---
    -   **Key:** `dict:index:category:<TYPE>` (e.g., `dict:index:category:PRODUCT`)
    -   **Description:** A separate Set is maintained for each `ClassificationType`. Each set stores the IDs of all codes belonging to that category. This allows for fast retrieval of all codes of a specific type.

### Time-to-Live (TTL)

- Cache write operations set or refresh a **30-day TTL** on the shared hash and the index keys
  they touch. Reads do not refresh expiration. The TTL applies to whole keys rather than
  individual codes. If an index references an expired/missing object, the read falls back
  to the database rather than returning a partial result.

---

## Database Migrations

Database schema changes are managed by [Liquibase](https://www.liquibase.org/). Migration scripts are located in `src/main/resources/liquibase/changelog/`.

- To add a new migration, create a new YAML file in the changelog directory and include it in `changelog-master.yaml`.

## Running Tests

With Docker running, execute:

```bash
./gradlew check
```

Five Testcontainers regression tests use an isolated Redis 7 container and the application's
Redis serializers and cache implementation. They cover category-first reads, writes with a
cold cache, classification changes, missing hash entries, and replacement of a full snapshot.
All five fail against the previous implementation and pass with the fixes. The database
repository is mocked; these tests do not establish PostgreSQL transaction behavior or the
concurrency guarantees discussed above. CI runs the same command.
