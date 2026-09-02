# Product Management API

## Overview

The **Product Management API** is a production-style RESTful API built using **Java 17** and **Spring Boot 3.x**. It provides comprehensive product and item CRUD capabilities, database-level pagination, stateless JWT authentication with refresh token rotation, fine-grained role-based access control (RBAC), standardized global exception handling, OpenAPI/Swagger documentation, and containerized deployment with Docker Compose.

---

## Features

- **Product CRUD Operations**: Full creation, retrieval, updating, and deletion of products.
- **Product Item Management**: Resource-oriented item collection management linked via foreign key relationships.
- **Database-Level Pagination**: Database-query level pagination for product collection endpoints using Spring Data `Pageable`.
- **Stateless JWT Authentication**: Secure access token issuance and validation.
- **Refresh Token Rotation**: Persistent refresh tokens with single-use rotation and explicit logout revocation.
- **Role-Based Authorization**: Distinct permissions for `ROLE_USER` and `ROLE_ADMIN`.
- **Input Validation**: Request payload validation using Jakarta Bean Validation.
- **Global Exception Handling**: Centralized `@RestControllerAdvice` returning standardized error responses.
- **Interactive API Documentation**: OpenAPI 3 specification accessible via Swagger UI with Bearer JWT test support.
- **Automated Testing Suite**: Mockito unit tests, Spring WebMvc controller tests, and full Spring Boot integration tests backed by an in-memory **H2** database.
- **Docker & Containerization**: Multi-stage `Dockerfile` and `docker-compose.yml` orchestrating Spring Boot and MySQL.

---

## Technology Stack

- **Core**: Java 17, Spring Boot 3.2.5
- **Web & Security**: Spring Web, Spring Security, JJWT (`0.11.5`), BCrypt
- **Persistence**: Spring Data JPA, Hibernate, MySQL 8.0 (`runtime`), H2 Database (`test`)
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit 5, Mockito, Spring Boot Test, Spring Security Test
- **Documentation**: OpenAPI 3 / SpringDoc Swagger UI (`2.5.0`)
- **Build & Containerization**: Maven, Docker, Docker Compose

---

## Architecture

```text
Client
  │
  ▼
Security Filter Chain (JwtAuthenticationFilter)
  │
  ▼
REST Controllers (ProductController, ItemController, AuthController)
  │
  ▼
Service Layer (ProductService, ItemService, AuthService)
  │
  ▼
Repository Layer (Spring Data JPA Repositories)
  │
  ▼
Database (MySQL / H2 for Tests)
```

The application strictly follows a layered architecture:
- **Controllers** handle HTTP request/response mappings, status codes, and routing.
- **Services** encapsulate core business logic, transactional boundaries, and token handling.
- **Repositories** manage database persistence abstractions via Spring Data JPA.
- **DTOs** decouple JPA entities from the REST API interface.

---

## Project Structure

```text
com.varad.productmanagement
├── config/       # Configuration classes (SecurityConfig, OpenApiConfig, DataInitializer)
├── controller/   # REST Controllers (AuthController, ProductController, ItemController)
├── service/      # Service interfaces and implementations (AuthService, ProductService, ItemService)
├── repository/   # Spring Data JPA Repositories (UserRepository, ProductRepository, ItemRepository, RefreshTokenRepository)
├── dto/          # Data Transfer Objects organized by domain (auth, product, item, common)
├── entity/       # JPA Entities (User, Product, Item, RefreshToken, Role)
├── security/     # JwtService, JwtAuthenticationFilter, CustomUserDetailsService
├── exception/    # Custom exceptions (ResourceNotFoundException, BadRequestException) and GlobalExceptionHandler
├── mapper/       # Manual Object Mappers (ProductMapper, ItemMapper)
└── util/         # Utility components (SecurityUtil)
```

### Package Responsibilities
- **`config/`**: Configures Spring Security filter chains, OpenAPI Swagger beans, and startup data seeding (`DataInitializer`).
- **`controller/`**: Exposes REST API endpoints and handles HTTP status codes and validation annotations (`@Valid`).
- **`service/`**: Implements business rules, transaction boundaries (`@Transactional`), pagination parameters, and security logic.
- **`repository/`**: Interfaces extending `JpaRepository` for DB access queries.
- **`dto/`**: Strongly-typed request/response payloads ensuring entities are never directly exposed.
- **`entity/`**: JPA persistent data models with annotations, indexing, constraints, and audit lifecycle callbacks (`@PrePersist`, `@PreUpdate`).
- **`security/`**: JWT creation/parsing, request filtering, and loading user security context.
- **`exception/`**: Global REST advice (`@RestControllerAdvice`) translating exceptions into standard JSON error bodies.
- **`mapper/`**: Helper components converting entities to DTOs and vice versa.
- **`util/`**: Shared security utilities (e.g. retrieving current authenticated user name).

---

## Authentication Flow

```text
Register / Login ────► Returns Access Token (Short-Lived) + Refresh Token (UUID, Persisted)
                             │
                             ▼
Access Protected API ───► Header: Authorization: Bearer <AccessToken>
                             │
                             ▼ (Token Expired)
Refresh Token Endpoint ──► POST /api/v1/auth/refresh { "refreshToken": "..." }
                             │
                             ├─► Old Refresh Token Revoked (rotates)
                             └─► Returns New Access Token + New Refresh Token
                             │
                             ▼
Logout ──────────────────► POST /api/v1/auth/logout { "refreshToken": "..." }
                             └─► Refresh Token Revoked (Cannot be reused)
```

1. **Register**: User submits credentials. Account is created with `ROLE_USER` by default.
2. **Login**: Credentials are validated via `AuthenticationManager`. An Access JWT and a unique persisted Refresh Token are returned.
3. **Access Token**: Short-lived JWT sent via `Authorization: Bearer <token>` header to access protected resources.
4. **Refresh Token Rotation**: When an access token expires, the client sends the active refresh token to `/api/v1/auth/refresh`. The backend revokes the old refresh token, generates a new access token, generates a new refresh token, and persists the new refresh token.
5. **Logout**: Calling `/api/v1/auth/logout` revokes the refresh token in the database.

---

## Roles & Authorization

The API supports two roles: **`ROLE_USER`** and **`ROLE_ADMIN`**.

| Endpoint Pattern | Method | Minimum Role Required | Description |
|---|---|---|---|
| `/api/v1/auth/**` | `POST` | Public | Auth operations (register, login, refresh, logout) |
| `/api/v1/products/**` | `GET` | `ROLE_USER` / `ROLE_ADMIN` | View products & items |
| `/api/v1/products/**` | `POST`, `PUT`, `DELETE` | `ROLE_ADMIN` | Product CRUD modifications |
| `/api/v1/products/{id}/items/**` | `POST`, `PUT`, `DELETE` | `ROLE_ADMIN` | Item CRUD modifications |

---

## Pagination

Product collection queries support database-level pagination:

```http
GET /api/v1/products?page=0&size=10
```

- **Default Page**: `0`
- **Default Size**: `10`
- **Maximum Size**: `100` (capped automatically to prevent memory exhaustion)

### Pagination Response Format

```json
{
  "content": [
    {
      "id": 1,
      "productName": "Wireless Gaming Mouse",
      "createdBy": "varad",
      "createdOn": "2026-09-02T10:30:00",
      "modifiedBy": "varad",
      "modifiedOn": "2026-09-02T10:30:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

---

## API Endpoints

### Authentication
- `POST /api/v1/auth/register` — Register a new user
- `POST /api/v1/auth/login` — Authenticate and obtain tokens
- `POST /api/v1/auth/refresh` — Refresh access token (Token Rotation)
- `POST /api/v1/auth/logout` — Logout and revoke refresh token

### Products
- `GET /api/v1/products` — Get paginated products (`USER`, `ADMIN`)
- `GET /api/v1/products/{id}` — Get product by ID (`USER`, `ADMIN`)
- `POST /api/v1/products` — Create product (`ADMIN` only)
- `PUT /api/v1/products/{id}` — Update product (`ADMIN` only)
- `DELETE /api/v1/products/{id}` — Delete product (`ADMIN` only)

### Items
- `GET /api/v1/products/{id}/items` — Get all items for a product (`USER`, `ADMIN`)
- `POST /api/v1/products/{id}/items` — Create item for a product (`ADMIN` only)
- `PUT /api/v1/products/{productId}/items/{itemId}` — Update item quantity (`ADMIN` only)
- `DELETE /api/v1/products/{productId}/items/{itemId}` — Delete item (`ADMIN` only)

---

## Environment Variables

Configure application settings dynamically via environment variables:

| Environment Variable | Description | Default Fallback Value |
|---|---|---|
| `DB_URL` | MySQL JDBC Connection URL | `jdbc:mysql://localhost:3306/product_db...` |
| `DB_USERNAME` | Database username | `root` |
| `DB_PASSWORD` | Database password | `root` |
| `JWT_SECRET` | Secret key for signing JWTs | *(Pre-configured 256-bit key)* |
| `JWT_EXPIRATION` | Access token expiration in ms | `86400000` (24 Hours) |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration in ms | `604800000` (7 Days) |

---

## 📷 Screenshots & Demo

Place your project screenshots in the [`docs/screenshots/`](file:///e:/Product-Management/docs/screenshots) directory:

| Feature | Screenshot |
|---|---|
| **Swagger UI Documentation** | ![Swagger UI](docs/screenshots/swagger-ui.png) |
| **Admin Login & JWT Token** | ![Admin Login](docs/screenshots/admin-login.png) |
| **Product & Item CRUD Operations** | ![Product CRUD](docs/screenshots/product-crud.png) |
| **Standardized Error Handling (400 Bad Request)** | ![Error Response](docs/screenshots/error-response.png) |

---

## Running Locally

### Prerequisites
1. **Java 17+** installed (`java -version`)
2. **Maven 3.8+** installed (`mvn -version`)
3. **MySQL 8.0** running locally on port 3306 (or configured via `DB_URL`)

### Steps
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
4. Access Swagger UI:  
   Open `http://localhost:8080/swagger-ui/index.html` in your browser.

> **Pre-seeded Admin User**: Upon startup, a default admin account is seeded:
> - **Username**: `varad`
> - **Password**: `varad@123`

---

## Running with Docker

Run the entire application (Spring Boot + MySQL) using Docker Compose:

```bash
docker compose up --build
```

- Docker Compose spins up MySQL, executes healthchecks, waits for MySQL readiness, builds the Spring Boot multi-stage Docker image, and starts the backend service on port `8080`.

To stop containers:
```bash
docker compose down
```

---

## Testing

Run the automated test suite (55+ unit and integration tests):

```bash
cd backend
mvn clean test
```

- **H2 In-Memory Database** is used automatically for all tests (via `application-test.properties` and `@ActiveProfiles("test")`), ensuring tests **never touch or depend on local MySQL instances**.

---

## Error Handling

All runtime exceptions and validation errors return a standardized JSON body via `GlobalExceptionHandler`:

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

---

## Security Notes

- **BCrypt Password Hashing**: Passwords are standardly hashed using `BCryptPasswordEncoder` before database persistence. Plaintext passwords are never logged or stored.
- **JWT Authentication**: Access tokens are signed using HMAC-SHA256 (`HS256`).
- **Refresh Token Rotation**: Refresh tokens are single-use tokens stored in the database. Each refresh request invalidates the previous refresh token and issues a new pair.
- **Environment-Based Secrets**: All sensitive properties (`DB_PASSWORD`, `JWT_SECRET`) are read from environment variables.
- **HTTPS Deployment Consideration**: In production deployments, TLS/HTTPS should be terminated at the reverse proxy (e.g. NGINX, AWS ALB, Cloudflare).
