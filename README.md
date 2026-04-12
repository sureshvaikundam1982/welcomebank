# Banking Microservices Project

A full-stack Spring Boot microservices banking application with React frontend.

## Architecture

```
banking_project/
├── config-server        (Port 8888) - Spring Cloud Config Server
├── eureka-server        (Port 8761) - Netflix Eureka Service Discovery
├── api-gateway          (Port 8090) - Spring Cloud Gateway + JWT Security
├── auth-service         (Port 8081) - User auth, JWT generation (H2 DB)
├── registration-service (Port 8082) - User registration (Feign → auth-service)
├── login-service        (Port 8083) - User login (Feign → auth-service)
├── account-service      (Port 8084) - Account management (H2 DB)
├── transaction-service  (Port 8085) - Transaction processing (H2 DB, Feign → account-service)
└── banking-frontend     (Port 3000) - React frontend
```

## Tech Stack

- **Backend**: Spring Boot 3.2.3, Spring Cloud 2023.0.0
- **Security**: Spring Security 6 + JWT (JJWT 0.12.3) — enforced at API Gateway
- **Database**: H2 In-Memory (per service)
- **Service Discovery**: Netflix Eureka
- **Config**: Spring Cloud Config (native profile)
- **Communication**: OpenFeign (with fallbacks)
- **API Docs**: SpringDoc OpenAPI 3 / Swagger UI
- **Frontend**: React 18, React Router 6, Axios

## Startup Order

Start services in this order:

### 1. Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
# Access: http://localhost:8761
```

### 2. Config Server
```bash
cd config-server
mvn spring-boot:run
# Access: http://localhost:8888
```

### 3. Auth Service
```bash
cd auth-service
mvn spring-boot:run
# H2 Console: http://localhost:8081/h2-console
# Swagger:    http://localhost:8081/swagger-ui.html
```

### 4. Registration Service
```bash
cd registration-service
mvn spring-boot:run
# Swagger: http://localhost:8082/swagger-ui.html
```

### 5. Login Service
```bash
cd login-service
mvn spring-boot:run
# Swagger: http://localhost:8083/swagger-ui.html
```

### 6. Account Service
```bash
cd account-service
mvn spring-boot:run
# H2 Console: http://localhost:8084/h2-console
# Swagger:    http://localhost:8084/swagger-ui.html
```

### 7. Transaction Service
```bash
cd transaction-service
mvn spring-boot:run
# H2 Console: http://localhost:8085/h2-console
# Swagger:    http://localhost:8085/swagger-ui.html
```

### 8. API Gateway
```bash
cd api-gateway
mvn spring-boot:run
# Gateway: http://localhost:8080
# Aggregated Swagger: http://localhost:8080/swagger-ui.html
```

### 9. React Frontend
```bash
cd banking-frontend
npm install
npm start
# App: http://localhost:3000
```

## Build All (from root)
```bash
mvn clean install -DskipTests
```

## API Endpoints (via Gateway at :8080)

### Public (no auth needed)
| Method | URL | Description |
|--------|-----|-------------|
| POST | /auth-service/api/auth/register | Register user |
| POST | /auth-service/api/auth/login | Login (direct) |
| POST | /registration-service/api/register | Register via registration-service |
| POST | /login-service/api/login | Login via login-service |
| POST | /auth-service/api/auth/validate | Validate JWT token |

### Protected (Bearer JWT required)
| Method | URL | Description |
|--------|-----|-------------|
| GET | /account-service/api/accounts | Get my accounts |
| POST | /account-service/api/accounts | Create account |
| GET | /account-service/api/accounts/{id} | Get account by ID |
| PUT | /account-service/api/accounts/balance | Update balance |
| DELETE | /account-service/api/accounts/{num}/close | Close account |
| GET | /transaction-service/api/transactions | Get my transactions |
| POST | /transaction-service/api/transactions | Process transaction |
| GET | /transaction-service/api/transactions/{id} | Get transaction |

## JWT Token Flow

1. Client calls `/login-service/api/login` → login-service calls auth-service via Feign
2. auth-service validates credentials, generates JWT
3. JWT returned to client
4. Client includes `Authorization: Bearer <token>` in requests
5. API Gateway JwtAuthenticationFilter validates token on every request
6. Gateway injects `X-Auth-Username` header for downstream services

## Service Communication (Feign)

```
registration-service → auth-service  (register user)
login-service        → auth-service  (authenticate)
transaction-service  → account-service (balance updates)
```

## H2 Database Consoles

| Service | URL | JDBC URL |
|---------|-----|----------|
| auth-service | http://localhost:8081/h2-console | jdbc:h2:mem:authdb |
| account-service | http://localhost:8084/h2-console | jdbc:h2:mem:accountdb |
| transaction-service | http://localhost:8085/h2-console | jdbc:h2:mem:transactiondb |

Username: `sa` | Password: `password`

## Swagger UI

- Auth Service: http://localhost:8081/swagger-ui.html
- Registration: http://localhost:8082/swagger-ui.html
- Login: http://localhost:8083/swagger-ui.html
- Account: http://localhost:8084/swagger-ui.html
- Transaction: http://localhost:8085/swagger-ui.html
- Aggregated (Gateway): http://localhost:8080/swagger-ui.html

## Eureka Dashboard

http://localhost:8761 (admin/admin)

## Transaction Types

- **DEPOSIT**: Add money to an account (`toAccountNumber` required)
- **WITHDRAWAL**: Remove money from an account (`fromAccountNumber` required)
- **TRANSFER**: Move money between accounts (both `fromAccountNumber` and `toAccountNumber` required)
