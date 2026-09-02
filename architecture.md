# System Architecture Document

## 1. High-Level Architecture Overview

The **Product Management API** is built following a clean, decoupled, layered architectural style. It enforces a strict separation of concerns across presentation, security, business logic, data persistence, and data transfer.

```mermaid
graph TD
    Client[Client / Postman / Swagger UI] -->|HTTP Requests| SecurityFilter[Spring Security Filter Chain]
    SecurityFilter -->|JWT Validation| JwtFilter[JwtAuthenticationFilter]
    JwtFilter -->|Authenticated Context| Controller[REST Controllers]
    
    subgraph Spring Boot Application
        Controller -->|DTOs| Service[Service Layer]
        Service -->|Entities| Repository[Spring Data JPA Repositories]
        Mapper[Mappers: ProductMapper / ItemMapper] <---> Controller
        Mapper <---> Service
    end
    
    subgraph Persistence Layer
        Repository -->|JDBC / ORM| Database[(MySQL Database / H2 Test DB)]
    end
```

---

## 2. Layered Architecture Breakdown

```text
com.varad.productmanagement
├── config/       # Spring Security, OpenAPI, Startup Data Seeding
├── controller/   # REST API Endpoints & Request Validation
├── service/      # Core Business Logic & Transaction Boundaries
├── repository/   # Data Access Interfaces (Spring Data JPA)
├── dto/          # Data Transfer Objects (Decoupled API Schemas)
├── entity/       # Persistent JPA Data Entities
├── security/     # Token Processing & UserDetailsService
├── exception/    # Custom Exceptions & Centralized Error Handler
├── mapper/       # Entity-to-DTO Mappers
└── util/         # Shared System Utilities
```

### 1. Presentation & Controller Layer (`controller/`)
- Exposes RESTful HTTP endpoints (`/api/v1/auth`, `/api/v1/products`, `/api/v1/products/{id}/items`).
- Validates request bodies using `@Valid` and Jakarta Validation annotations.
- Converts HTTP path variables and query parameters to Java types.
- Delegates business execution to service interfaces and returns standard `ResponseEntity` wrappers.

### 2. Security & Authentication Layer (`security/` & `config/`)
- Intercepts incoming requests using `JwtAuthenticationFilter` before reaching controller endpoints.
- Validates Bearer Access Tokens and populates Spring Security's `SecurityContextHolder`.
- Enforces role-based URL authorization rules (`ROLE_USER`, `ROLE_ADMIN`) in `SecurityConfig`.
- Hashes and verifies passwords securely using `BCryptPasswordEncoder`.

### 3. Business Logic Layer (`service/`)
- Enforces system constraints, transactional integrity (`@Transactional`), and business validation rules.
- Performs audit logging for entity mutations (`createdBy`, `modifiedBy`).
- Manages pagination parameters and enforces maximum page size limits (capped at 100).
- Implements single-use Refresh Token rotation logic.

### 4. Data Access & Persistence Layer (`repository/` & `entity/`)
- Extends Spring Data `JpaRepository` interfaces for zero-boilerplate DB queries.
- Manages database schemas, constraints, foreign key relationships, and indexes using JPA/Hibernate annotations.
- Utilizes `@PrePersist` and `@PreUpdate` entity lifecycle callbacks to automate creation and modification timestamps.

---

## 3. Entity Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o{ REFRESH_TOKENS : "owns"
    PRODUCTS ||--o{ ITEMS : "contains"

    USERS {
        bigint id PK
        varchar username UK
        varchar email UK
        varchar password
        enum role "ROLE_USER, ROLE_ADMIN"
    }

    REFRESH_TOKENS {
        bigint id PK
        varchar token UK
        bigint user_id FK
        datetime expiry
        boolean revoked
        datetime created_on
    }

    PRODUCTS {
        bigint id PK
        varchar product_name
        varchar created_by
        datetime created_on
        varchar modified_by
        datetime modified_on
    }

    ITEMS {
        bigint id PK
        bigint product_id FK
        int quantity
    }
```

---

## 4. Authentication & Token Rotation Flow

```mermaid
sequenceDiagram
    autonumber
    actor Client
    participant API as Auth Controller
    participant Service as Auth Service
    participant DB as Database (MySQL)

    Note over Client, DB: Registration / Login
    Client->>API: POST /api/v1/auth/login { username, password }
    API->>Service: Authenticate credentials
    Service->>DB: Fetch User & verify BCrypt hash
    Service->>Service: Generate short-lived Access JWT & persistent Refresh Token (UUID)
    Service->>DB: Save RefreshToken { token, expiry, revoked=false }
    Service-->>Client: Return { accessToken, refreshToken, role }

    Note over Client, DB: Accessing Protected Endpoints
    Client->>API: GET /api/v1/products (Header: Authorization Bearer <accessToken>)
    API-->>Client: 200 OK (Paginated Products)

    Note over Client, DB: Refresh Token Rotation
    Client->>API: POST /api/v1/auth/refresh { refreshToken }
    API->>Service: Validate RefreshToken
    Service->>DB: Query RefreshToken by token
    Service->>Service: Verify token is NOT expired AND NOT revoked
    Service->>DB: Set old RefreshToken revoked=true
    Service->>DB: Save new RefreshToken (UUID)
    Service-->>Client: Return New { accessToken, refreshToken }

    Note over Client, DB: Logout
    Client->>API: POST /api/v1/auth/logout { refreshToken }
    API->>Service: Revoke RefreshToken
    Service->>DB: Set RefreshToken revoked=true
    Service-->>Client: 200 OK "Logged out successfully"
```

---

## 5. Security & Authorization Matrix

| Endpoint | Method | Public | `ROLE_USER` | `ROLE_ADMIN` |
|---|---|:---:|:---:|:---:|
| `/api/v1/auth/**` | `POST` | ✅ | ✅ | ✅ |
| `/v3/api-docs/**`, `/swagger-ui/**` | `GET` | ✅ | ✅ | ✅ |
| `/api/v1/products` | `GET` | ❌ | ✅ | ✅ |
| `/api/v1/products/{id}` | `GET` | ❌ | ✅ | ✅ |
| `/api/v1/products` | `POST` | ❌ | ❌ | ✅ |
| `/api/v1/products/{id}` | `PUT` | ❌ | ❌ | ✅ |
| `/api/v1/products/{id}` | `DELETE` | ❌ | ❌ | ✅ |
| `/api/v1/products/{id}/items` | `GET` | ❌ | ✅ | ✅ |
| `/api/v1/products/{id}/items` | `POST` | ❌ | ❌ | ✅ |
| `/api/v1/products/{id}/items/{itemId}` | `PUT` | ❌ | ❌ | ✅ |
| `/api/v1/products/{id}/items/{itemId}` | `DELETE` | ❌ | ❌ | ✅ |

---

## 6. Exception & Error Response Architecture

All system exceptions are captured by `GlobalExceptionHandler` (`@RestControllerAdvice`) and converted into a standard error response structure:

```json
{
  "timestamp": "2026-09-02T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/products",
  "errors": {
    "productName": "Product name is required"
  }
}
```

### Exception Mapping Table
| Exception Class | Response Status | Description |
|---|---|---|
| `ResourceNotFoundException` | `404 Not Found` | Entity missing in database (e.g. Product or Item ID not found) |
| `BadRequestException` | `400 Bad Request` | Invalid credentials, duplicate username/email, expired/revoked refresh token |
| `MethodArgumentNotValidException` | `400 Bad Request` | Field-level validation failure (mapped in `errors` object) |
| `BadCredentialsException` | `400 Bad Request` | Invalid login credentials |
| `AccessDeniedException` | `403 Forbidden` | Authenticated user lacks required role (`ROLE_USER` calling ADMIN endpoint) |
| `AuthenticationException` | `401 Unauthorized` | Missing or invalid JWT access token |

---

## 7. Deployment & Infrastructure Architecture

```mermaid
graph LR
    subgraph Docker Host
        subgraph Bridge Network: product-management-network
            Backend[Backend Container: Spring Boot App :8080]
            Database[(MySQL 8.0 Container :3306)]
        end
    end

    Client[HTTP Client / Browser] -->|Port 8080| Backend
    Backend -->|Healthcheck Dependency| Database
```

- **Containerization**: Multi-stage Docker build using `maven:3.9.6` for packaging and `eclipse-temurin:17-jre-alpine` for execution.
- **Orchestration**: `docker-compose.yml` configures service dependencies where the backend container waits for MySQL's native `healthcheck` (`mysqladmin ping`) to report `healthy` before launching.
