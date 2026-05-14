# Production Auth Implementation Prompts

Bu sənəd [PROD_AUTH_PLAN.md](C:/Users/Administrator/Desktop/ecommerce-auth/myplan/PROD_AUTH_PLAN.md) əsasında hazırlanmış step-by-step prompt paketidir. Məqsəd bu promptları başqa bir AI agentə verib production-səviyyəli auth sistemini mərhələli şəkildə yazdırmaqdır.

İstifadə qaydası:

- promptları sırayla istifadə et
- hər step bitəndən sonra kodu review et
- növbəti step-ə keçməzdən əvvəl əvvəlki step compile və test olunsun
- AI-dan yalnız kod yox, qərarların qısa izahını da istə
- AI-a mövcud faylları pozmamağı və unrelated dəyişiklik etməməyi tapşır

## Global master prompt

Bu promptu bütün mərhələlərdən əvvəl ver:

```text
You are a senior backend engineer building a production-grade authentication and authorization system in Spring Boot with PostgreSQL.

Your task is to implement the auth system step by step based on the provided plan. Follow these rules strictly:

1. Preserve existing project structure unless a change is clearly justified.
2. Do not remove unrelated code.
3. Use Spring Boot, Spring Security, JPA, PostgreSQL, Flyway, and JWT.
4. Include Swagger UI / OpenAPI documentation support for auth endpoints.
5. Prefer clean architecture, small focused classes, and clear naming.
6. Use DTOs for requests and responses.
7. Use validation annotations where appropriate.
8. Use hashed passwords only, never plain text.
9. Use hashed refresh tokens in database, never plain text refresh tokens.
10. Return access token in response body, but deliver refresh token only via HttpOnly Secure cookie unless a step explicitly says otherwise.
11. Treat browser security seriously: cookie attributes, HTTPS assumptions, CORS, and CSRF implications must be considered.
12. Explain what you changed after each step.
13. At the end of each step, list created or modified files.
14. If configuration values are needed, place them in application config and clearly mark secrets that should come from environment variables.
15. If a step depends on a previous missing piece, implement the missing prerequisite cleanly.
16. Write code in a production-minded way, not tutorial style.
17. Add tests when the step naturally requires them.
18. If you make assumptions, state them clearly.

When responding:
- first summarize the step goal in 2-4 lines
- then implement the code
- then provide a short change summary
- then list files changed
- then mention how to test the step
```

## Level 1

### Goal

Project foundation və dependency hazırlığı.

### Prompt

```text
Implement Step 1 of a production-grade auth system in an existing Spring Boot project.

Step goal:
- prepare the project foundation for authentication and authorization
- add the required dependencies
- prepare configuration structure
- prepare package layout for auth, security, user, token, and exception handling

Requirements:
- add Spring Security dependency
- add JWT library dependencies
- add or keep Swagger/OpenAPI dependency so endpoints can be tested from Swagger UI
- ensure Flyway is used for schema migration
- prepare application configuration keys for JWT secret, access token expiration, refresh token expiration
- prepare configuration keys for secure cookie behavior where appropriate
- do not hardcode secrets in source code
- create a clean package structure for:
  - controller
  - service
  - security
  - entity
  - repository
  - dto
  - exception
- keep the implementation compile-friendly
- do not yet implement full auth logic unless required for compilation

Deliverables:
- updated build file
- initial config structure
- any placeholder classes needed for the next steps
- Swagger/OpenAPI foundation if missing

After implementation:
- explain the dependencies added
- list all created or modified files
- explain how this prepares the project for the next step
```

## Level 2

### Goal

User, role və refresh token domain modelini qurmaq.

### Prompt

```text
Implement Step 2 of a production-grade auth system in Spring Boot.

Step goal:
- create the core domain model for authentication and authorization

Requirements:
- create User entity
- create Role entity or enum-based role model, but choose the more extensible production-friendly design
- create RefreshToken entity
- include fields typically needed in production:
  - User: id, email, username, passwordHash, enabled, accountNonLocked, emailVerified, failedLoginAttempts, lastLoginAt, createdAt, updatedAt
  - RefreshToken: id, user reference, tokenHash, expiresAt, createdAt, revoked, revokedAt, replacedByTokenHash, userAgent, ipAddress, deviceName
- apply proper constraints:
  - unique email
  - unique username
  - not null where appropriate
- model user-role relationship cleanly
- add auditing timestamps if suitable
- keep naming explicit and maintainable

Deliverables:
- entities
- enums if needed
- repositories for new auth entities

After implementation:
- explain the entity relationships
- list changed files
- mention any assumptions you made
```

## Level 3

### Goal

Flyway migration və database schema qurmaq.

### Prompt

```text
Implement Step 3 of a production-grade auth system in Spring Boot.

Step goal:
- create Flyway migrations for the auth schema

Requirements:
- write SQL migrations for:
  - users
  - roles
  - user_roles
  - refresh_tokens
- add proper primary keys, foreign keys, unique constraints, and useful indexes
- make the schema PostgreSQL-friendly
- include timestamps and boolean defaults where needed
- ensure the schema matches the existing entity model
- prefer explicit SQL over implicit schema generation
- do not rely on hibernate ddl-auto for auth tables

Deliverables:
- Flyway migration files
- any config changes needed to activate Flyway properly

After implementation:
- explain the schema choices
- list files changed
- explain how to run migrations safely
```

## Level 4

### Goal

Security skeleton və JWT service əsasını qurmaq.

### Prompt

```text
Implement Step 4 of a production-grade auth system in Spring Boot.

Step goal:
- create the security foundation and JWT support classes

Requirements:
- create SecurityConfig
- configure stateless security
- permit public auth endpoints
- require authentication for protected endpoints
- prepare role-based authorization support
- prepare for access token via Authorization header and refresh token via secure cookie
- create JwtService responsible for:
  - generating access tokens
  - parsing tokens
  - validating token type
  - reading claims
- include claims such as:
  - sub
  - roles
  - token_type
  - iat
  - exp
  - jti
- do not yet implement refresh endpoint logic unless needed
- create CustomUserDetailsService integrated with User repository
- add PasswordEncoder bean
- if Swagger/OpenAPI is present, configure bearer auth documentation

Deliverables:
- security config
- jwt service
- user details service
- supporting security classes if needed

After implementation:
- explain request flow through security
- list changed files
- mention what still remains before login works
```

## Level 5

### Goal

Register flow-u production-a yaxın formada yazmaq.

### Prompt

```text
Implement Step 5 of a production-grade auth system in Spring Boot.

Step goal:
- implement user registration

Requirements:
- create RegisterRequest DTO with validation
- create Auth or Registration response DTOs
- implement register endpoint: POST /api/auth/register
- validate unique email and username
- hash password with PasswordEncoder
- assign default USER role
- persist the user safely
- do not expose password hash in responses
- return a clean API response
- implement business exceptions for duplicate email or username
- keep code layered: controller, service, repository, dto, exception

Optional if architecture already supports it cleanly:
- create email verification token skeleton without full mail integration yet

Swagger requirement:
- ensure the register endpoint is clearly documented in Swagger UI

After implementation:
- explain the request-to-database flow
- list changed files
- explain how to test registration with example payload
```

## Level 6

### Goal

Login və access token + refresh token creation.

### Prompt

```text
Implement Step 6 of a production-grade auth system in Spring Boot.

Step goal:
- implement login and token issuance

Requirements:
- create LoginRequest DTO
- implement POST /api/auth/login
- authenticate using email or username plus password
- check account status:
  - enabled
  - accountNonLocked
  - optionally emailVerified if the design enforces verification before login
- on failed login, increment failedLoginAttempts
- on too many failed attempts, lock the account if your design supports it
- on successful login:
  - reset failedLoginAttempts
  - update lastLoginAt
  - generate short-lived access token
  - generate refresh token
  - hash the refresh token before storing it
  - persist refresh token record with metadata fields if available
- return:
  - accessToken
  - accessTokenExpiresAt
  - refreshTokenExpiresAt
  - tokenType

Requirements for token design:
- access token must be JWT
- refresh token must be securely random
- refresh token plain value must not be returned in response body
- refresh token must be written as HttpOnly Secure cookie
- set cookie attributes explicitly, including SameSite and path
- assume HTTPS usage for secure cookie behavior

Swagger requirement:
- document that login returns access token in body and sets refresh token via Set-Cookie

After implementation:
- explain login success and failure behavior
- list changed files
- show how to test login and inspect the tokens
```

## Level 7

### Goal

JWT filter və protected endpoint authorization.

### Prompt

```text
Implement Step 7 of a production-grade auth system in Spring Boot.

Step goal:
- protect endpoints using JWT access tokens

Requirements:
- create JwtAuthenticationFilter
- read Authorization header with Bearer token
- validate token using JwtService
- ensure only access tokens are accepted for request authentication
- load user using CustomUserDetailsService
- populate SecurityContext
- configure SecurityConfig to register the filter correctly
- create at least one protected endpoint for authenticated users
- create at least one admin-only endpoint using role-based authorization
- ensure unauthorized and forbidden responses are handled clearly
- make the protected endpoints testable from Swagger UI using bearer auth

Deliverables:
- jwt auth filter
- protected sample endpoints or secured existing endpoints
- security exception handling if needed

After implementation:
- explain the authentication flow for an incoming request
- list changed files
- explain how to test authenticated and unauthorized cases
```

## Level 8

### Goal

Refresh token rotation və logout session idarəsi.

### Prompt

```text
Implement Step 8 of a production-grade auth system in Spring Boot.

Step goal:
- implement refresh token rotation and logout flows

Requirements:
- create POST /api/auth/refresh
- read refresh token from HttpOnly Secure cookie
- hash incoming refresh token before lookup
- validate:
  - token exists
  - token is not revoked
  - token is not expired
  - related user is active
- rotate refresh token on successful refresh:
  - revoke old token
  - record revokedAt
  - record replacedByTokenHash if your model supports it
  - generate new access token
  - generate new refresh token
  - store new hashed refresh token
- set a new refresh cookie during rotation
- create POST /api/auth/logout
- revoke the refresh token from cookie during logout
- clear the refresh cookie during logout
- create POST /api/auth/logout-all
- revoke all active refresh tokens for the current user
- clear the current refresh cookie during logout-all

Important:
- do not store refresh token plain text in the database
- keep the implementation production-minded and explicit
- keep cookie handling centralized and consistent

After implementation:
- explain token rotation logic
- explain how logout and logout-all differ
- list changed files
- explain how to test refresh and revocation scenarios
```

## Level 9

### Goal

Forgot password, reset password və email verification.

### Prompt

```text
Implement Step 9 of a production-grade auth system in Spring Boot.

Step goal:
- implement password reset and email verification flows

Requirements:
- create PasswordResetToken entity and persistence model
- create EmailVerificationToken entity and persistence model
- create Flyway migrations if new tables are needed
- implement:
  - POST /api/auth/forgot-password
  - POST /api/auth/reset-password
  - POST /api/auth/verify-email
  - optionally POST /api/auth/resend-verification
- forgot-password must return a generic response to avoid account enumeration
- reset-password must:
  - validate token
  - reject expired or used tokens
  - update password hash
  - mark token used
  - revoke all active refresh tokens for that user
- verify-email must:
  - validate token
  - mark emailVerified=true
  - mark verification token used
- if no real mail service exists yet, design the flow so email sending can be plugged in later cleanly

Swagger requirement:
- document these endpoints clearly in Swagger UI with realistic request examples

After implementation:
- explain security considerations in these flows
- list changed files
- explain how to test the flow without a real email provider
```

## Level 10

### Goal

Production hardening, audit, exception handling və test coverage.

### Prompt

```text
Implement Step 10 of a production-grade auth system in Spring Boot.

Step goal:
- harden the auth system for production readiness

Requirements:
- add structured exception handling for auth-related errors
- standardize API error responses
- add audit logging for:
  - successful login
  - failed login
  - token refresh
  - logout
  - password reset request
  - password reset success
  - role change if such endpoint exists
- add integration tests for:
  - register
  - login
  - protected endpoint access
  - refresh token rotation
  - logout
  - forgot password
  - reset password
- verify Set-Cookie attributes in login and refresh responses
- verify cookie clearing behavior in logout responses
- add unit tests where helpful for JWT or token utilities
- improve security configuration if needed for clear unauthorized and forbidden responses
- review config so secrets are externalized
- identify any missing production gaps such as rate limiting, lockout tuning, CORS, cookie strategy, monitoring, or secret rotation, and summarize them clearly if not implemented
- review whether Swagger UI should be restricted or disabled in production

Deliverables:
- robust error handling
- audit logging support
- meaningful test coverage
- short production-readiness review

After implementation:
- summarize remaining risks or next recommended improvements
- list changed files
- explain how to run the tests
```

## Extra final prompt

Bu promptu `Level 10`-dan sonra verə bilərsən:

```text
Review the full authentication system implementation as a production-focused senior backend engineer.

Please do all of the following:
- identify security weaknesses
- identify missing edge cases
- identify schema or token lifecycle risks
- identify API design improvements
- identify testing gaps
- identify configuration and deployment concerns
- propose a prioritized improvement list

Return the review in this order:
1. critical risks
2. medium risks
3. missing tests
4. operational concerns
5. recommended next steps
```

## İstifadə strategiyası

Ən yaxşı nəticə üçün bu qaydanı izlə:

1. əvvəl `Global master prompt`
2. sonra yalnız bir `Level` prompt ver
3. AI cavabını yoxla
4. compile və test et
5. növbəti level-ə keç

## Qısa tövsiyə

Əgər AI çox böyük dəyişiklik etməyə meyllidirsə, hər promptun əvvəlinə bunu əlavə et:

```text
Work only on the current step. Do not implement future steps yet. Keep changes scoped and production-quality.
```
