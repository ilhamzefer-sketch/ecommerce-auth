# E-commerce Production Authentication System

This project is a professional-grade, secure authentication and authorization system built with Spring Boot 3.

## Features Implemented

1.  **Level 1: Project Foundation**
    *   JJWT for token management.
    *   Flyway for database migrations.
    *   SpringDoc OpenAPI (Swagger) for documentation.
2.  **Level 2: Domain Model**
    *   User, Role, and RefreshToken entities.
    *   Auditing (createdAt, updatedAt).
3.  **Level 3: Database Schema**
    *   Structured SQL migrations for users, roles, and tokens.
    *   Indeces for performance.
4.  **Level 4: Security Skeleton**
    *   Stateless session management.
    *   BCrypt password hashing.
    *   JWT Service for generation/validation.
5.  **Level 5: Registration**
    *   Secure user registration with validation.
    *   Unique constraint handling.
6.  **Level 6: Login & Token Issuance**
    *   Access Token (JWT) in body.
    *   Refresh Token (Hashed) in HttpOnly Secure Cookie.
7.  **Level 7: Authorization**
    *   JWT Authentication Filter.
    *   Role-based access control (USER, ADMIN).
8.  **Level 8: Session Management**
    *   Refresh Token Rotation.
    *   Reuse detection and session invalidation.
    *   Logout and Logout-All functionality.
9.  **Level 9: Account Recovery**
    *   Forgot/Reset password flow (with session revocation).
    *   Email verification tokens.
10. **Level 10: Quality Assurance**
    *   Slf4j Logging for security events.
    *   Unit tests for core logic.

## How to Run

1.  Configure your database in `src/main/resources/application.yaml`.
2.  Set your `JWT_SECRET_KEY` (minimum 32 characters).
3.  Run the application using `./gradlew bootRun`.
4.  Access Swagger UI through the API Gateway at: `http://localhost:8080/swagger-ui/index.html`

## Testing

Run unit tests:
```bash
./gradlew test
```
