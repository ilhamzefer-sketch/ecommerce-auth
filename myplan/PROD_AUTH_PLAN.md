# Production Auth Plan

Bu sənəd `ecommerce-auth` layihəsi üçün production-səviyyəli authentication və authorization planıdır. Məqsəd yalnız `login/register` yazmaq deyil, təhlükəsiz, genişlənə bilən və real sistemə uyğun auth arxitekturası qurmaqdır.

## Məqsəd

Sistem aşağıdakı imkanları verməlidir:

- user registration
- login
- access token
- refresh token
- logout
- forgot password
- reset password
- email verification
- role-based access control
- audit və security logging
- token revocation
- session/device management

## Ümumi yanaşma

Tövsiyə olunan arxitektura:

- backend: Spring Boot
- database: PostgreSQL
- auth: JWT access token + refresh token
- password hashing: BCrypt və ya Argon2
- migration: Flyway
- API protection: Spring Security
- API documentation and testing: Swagger UI / OpenAPI

Əsas prinsip:

- `access token` qısaömürlü olur
- `refresh token` daha uzunömürlü olur
- refresh token database-də izlənir
- logout və compromise halında token revoke edilə bilir
- browser mühiti üçün refresh token `HttpOnly Secure` cookie-də saxlanılır

## Token strategiyası

### Access token

İstifadə məqsədi:

- hər request-də user-i identifikasiya etmək
- qorunan endpoint-lərə giriş vermək

Xüsusiyyətlər:

- stateless JWT
- qısa ömür: məsələn `10-15 dəqiqə`
- içində minimum claim saxlanılır
- response body-də qaytarılır
- client tərəfindən `Authorization: Bearer <token>` ilə göndərilir

Access token daxilində ola biləcək claim-lər:

- `sub`: user id və ya username
- `email`
- `roles`
- `token_type=access`
- `iat`
- `exp`
- `jti`

Access token-da bunları saxlamaq məsləhət deyil:

- password
- həssas PII
- böyük həcmli məlumat
- dəyişkən biznes state

### Refresh token

İstifadə məqsədi:

- access token bitəndə yenisini almaq

Xüsusiyyətlər:

- uzun ömür: məsələn `7-30 gün`
- database-də saxlanılır
- hər refresh zamanı rotate olunur
- reuse detection dəstəklənir
- browser ssenarisində `HttpOnly`, `Secure`, `SameSite` cookie ilə verilir

Refresh token üçün təhlükəsiz yanaşma:

- token plain text kimi DB-də saxlanmır
- hash olunmuş formada saxlanılır
- `expires_at`, `revoked`, `revoked_at`, `device_info`, `ip_address` izlənir
- cookie atributları environment-a uyğun idarə olunur

## Niyə access və refresh token birlikdə?

Yalnız uzunömürlü JWT istifadə etmək risklidir. Token oğurlansa, uzun müddət istifadə olunar. Ona görə:

- access token qısaömürlü olur
- refresh token server tərəfindən idarə olunur
- refresh zamanı session nəzarəti qorunur

Bu model həm təhlükəsizlik, həm də istifadəçi təcrübəsi baxımından balanslıdır.

## Tövsiyə olunan modullar

- `auth`
- `user`
- `role`
- `security`
- `token`
- `mail`
- `audit`

## Package strukturu

- `az.ilham.ecommerceauth.controller`
- `az.ilham.ecommerceauth.service`
- `az.ilham.ecommerceauth.security`
- `az.ilham.ecommerceauth.entity`
- `az.ilham.ecommerceauth.repository`
- `az.ilham.ecommerceauth.dto`
- `az.ilham.ecommerceauth.exception`
- `az.ilham.ecommerceauth.mapper`
- `az.ilham.ecommerceauth.audit`

Daha təmiz struktur istəsən belə də bölə bilərsən:

- `controller.auth`
- `controller.user`
- `service.auth`
- `service.user`
- `security.jwt`
- `security.filter`
- `dto.auth.request`
- `dto.auth.response`

## Əsas entity-lər

### `User`

Sahələr:

- `id`
- `email`
- `username`
- `passwordHash`
- `firstName`
- `lastName`
- `enabled`
- `accountNonLocked`
- `emailVerified`
- `failedLoginAttempts`
- `lastLoginAt`
- `createdAt`
- `updatedAt`

Qaydalar:

- `email` unique
- `username` unique
- password yalnız hash kimi saxlanılır

### `Role`

Minimal variant:

- `USER`
- `ADMIN`

Genişlənən variant:

- ayrıca `roles` cədvəli
- `user_roles` join table

### `RefreshToken`

Sahələr:

- `id`
- `userId`
- `tokenHash`
- `expiresAt`
- `createdAt`
- `revoked`
- `revokedAt`
- `replacedByTokenHash`
- `deviceName`
- `userAgent`
- `ipAddress`

Bu entity production üçün çox vacibdir. Çünki session idarəsi məhz burada olur.

### `PasswordResetToken`

Sahələr:

- `id`
- `userId`
- `tokenHash`
- `expiresAt`
- `used`
- `usedAt`
- `createdAt`

### `EmailVerificationToken`

Sahələr:

- `id`
- `userId`
- `tokenHash`
- `expiresAt`
- `used`
- `usedAt`

## API endpoint planı

### Public auth endpoint-lər

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `POST /api/auth/logout-all`
- `POST /api/auth/forgot-password`
- `POST /api/auth/reset-password`
- `POST /api/auth/verify-email`
- `POST /api/auth/resend-verification`

### User endpoint-lər

- `GET /api/users/me`
- `PATCH /api/users/me`
- `PATCH /api/users/me/password`
- `GET /api/users/me/sessions`
- `DELETE /api/users/me/sessions/{sessionId}`

### Admin endpoint-lər

- `GET /api/admin/users`
- `GET /api/admin/users/{id}`
- `PATCH /api/admin/users/{id}/roles`
- `PATCH /api/admin/users/{id}/lock`
- `PATCH /api/admin/users/{id}/unlock`
- `PATCH /api/admin/users/{id}/disable`
- `PATCH /api/admin/users/{id}/enable`

## Auth flow-lar

### 1. Register flow

Axın:

1. user `email`, `username`, `password` göndərir
2. input validation edilir
3. duplicate `email/username` yoxlanılır
4. password hash olunur
5. user `enabled=true`, `emailVerified=false` ilə yaradılır
6. default role `USER` verilir
7. email verification token yaradılır
8. verification email göndərilir
9. response qaytarılır

Tövsiyə:

- register response içində password qaytarılmır
- email mövcud olsa belə leakage etməmək üçün ümumi mesaj istifadə oluna bilər

### 2. Login flow

Axın:

1. user `email/username + password` göndərir
2. user tapılır
3. account status yoxlanılır
4. password hash compare edilir
5. uğursuzdursa failed attempt artırılır
6. limit keçibsə account müvəqqəti lock olunur
7. uğurludursa failed attempts sıfırlanır
8. access token yaradılır
9. refresh token yaradılır
10. refresh token hash olunaraq DB-yə yazılır
11. login audit log yazılır
12. response qaytarılır

Login response:

- `accessToken`
- `accessTokenExpiresAt`
- `refreshTokenExpiresAt`
- `tokenType`

Qeyd:

- `refreshToken` response body-də qaytarılmır
- `refreshToken` `Set-Cookie` ilə `HttpOnly Secure` cookie kimi yazılır

### 3. Refresh flow

Axın:

1. client refresh token göndərir
1.1 browser ssenarisində refresh token `HttpOnly` cookie-dən oxunur
2. token hash edilir
3. DB-də uyğun aktiv refresh token axtarılır
4. revoked və expired olub-olmadığı yoxlanılır
5. user status yoxlanılır
6. köhnə refresh token revoke olunur
7. yeni access token yaradılır
8. yeni refresh token yaradılır
9. yeni refresh token DB-yə yazılır
10. response qaytarılır

Production vacib detal:

- refresh token rotation olmalıdır
- köhnə refresh token yenidən istifadə edilərsə compromise kimi qəbul edilə bilər

### 4. Logout flow

Axın:

1. client refresh token və ya session id göndərir
2. həmin refresh token revoke olunur
3. `revoked=true`, `revokedAt=now` yazılır
4. istifadəçi həmin session-dan çıxmış sayılır
5. refresh cookie browser-də clear edilir

### 5. Logout all sessions flow

Axın:

1. user-in bütün aktiv refresh token-ləri tapılır
2. hamısı revoke olunur
3. bütün cihazlardan çıxış edilir
4. cari refresh cookie də clear edilir

### 6. Forgot password flow

Axın:

1. user email göndərir
2. email varsa reset token yaradılır
3. token hash olunmuş formada saxlanılır
4. email vasitəsilə reset link göndərilir
5. response hər halda generic olur

Vacibdir:

- “bu email tapılmadı” kimi response vermə
- account enumeration riskini azaldır

### 7. Reset password flow

Axın:

1. user token + new password göndərir
2. token hash edilir və DB-də axtarılır
3. token expired/used olub-olmadığı yoxlanılır
4. yeni password policy yoxlanılır
5. password yenilənir
6. reset token `used=true` olur
7. mövcud bütün refresh token-lər revoke edilir
8. audit log yazılır

### 8. Email verification flow

Axın:

1. verify token qəbul olunur
2. DB-də yoxlanılır
3. istifadə edilməyibsə və expired deyilsə qəbul olunur
4. user `emailVerified=true` olur
5. token used kimi işarələnir

## Authorization modeli

Tövsiyə olunan ilk mərhələ:

- `ROLE_USER`
- `ROLE_ADMIN`

Daha sonra permission səviyyəsi əlavə edilə bilər:

- `USER_READ`
- `USER_WRITE`
- `ORDER_READ`
- `ORDER_WRITE`

Başlanğıc üçün role-based model kifayətdir. Permission-based model sonradan əlavə oluna bilər.

## Spring Security planı

Yazılacaq əsas komponentlər:

- `SecurityConfig`
- `JwtAuthenticationFilter`
- `JwtService`
- `CustomUserDetailsService`
- `AuthenticationService`
- `RefreshTokenService`
- `PasswordResetService`

### `SecurityConfig`

Burada:

- public endpoint-lər `permitAll`
- admin endpoint-lər `hasRole("ADMIN")`
- qalanları authenticated
- session policy `STATELESS`
- access token bearer header ilə getdiyi üçün stateless auth qorunur
- refresh token cookie ilə işlədiyi üçün CSRF strategiyası ayrıca düşünülür

### `JwtAuthenticationFilter`

İş prinsipi:

1. `Authorization: Bearer <token>` oxunur
2. token parse edilir
3. `token_type=access` yoxlanılır
4. `exp` yoxlanılır
5. user load edilir
6. `SecurityContext` doldurulur

### `CustomUserDetailsService`

Spring Security üçün user-i DB-dən yükləyir.

## Database planı

Flyway migration-ları mərhələli yaz:

1. `users`
2. `roles`
3. `user_roles`
4. `refresh_tokens`
5. `password_reset_tokens`
6. `email_verification_tokens`
7. audit cədvəlləri

## Password policy

Minimum tələblər:

- ən azı `8-12` simvol
- böyük hərf
- kiçik hərf
- rəqəm
- xüsusi simvol

Əlavə qaydalar:

- çox zəif və məşhur parollar bloklansın
- son N şifrənin təkrar istifadəsi istəyə görə bloklana bilər

Hashing üçün:

- `BCrypt` yaxşı başlanğıcdır
- daha güclü yanaşma kimi `Argon2` də düşünülə bilər

## Security best practices

### 1. Password plain text saxlanmır

Yalnız hash saxlanılır.

### 2. Refresh token DB-də plain text saxlanmır

Yalnız hash saxlanılır.

### 3. Token expiry qısa və məqsədəuyğun seçilir

- access token: `10-15 dəqiqə`
- refresh token: `7-30 gün`

### 4. Rate limiting

Bu endpoint-lərdə rate limit düşün:

- `/login`
- `/register`
- `/forgot-password`
- `/refresh`

### 5. Brute force protection

- failed login counter
- temporary lock
- captcha sonradan əlavə oluna bilər

### 6. Audit logging

Log et:

- successful login
- failed login
- password reset request
- password reset success
- email verification
- logout
- role change
- account lock/unlock

### 7. Secret management

JWT secret source code içində saxlanmamalıdır.

İstifadə et:

- environment variable
- secret manager
- secure config source

### 8. CORS və cookie strategiyası

Əgər frontend ayrıdırsa:

- düzgün CORS config ver
- `allowCredentials=true` yalnız lazım olan origin-lər üçün aç
- wildcard origin ilə credential istifadəsi etmə

Əgər refresh token cookie-də saxlanacaqsa:

- `HttpOnly`
- `Secure`
- `SameSite`
- `Path` mümkün qədər məhdud tutulur, məsələn `/api/auth/refresh`
- production-da yalnız HTTPS üzərindən işləyir

Əgər JSON body ilə qaytarılacaqsa:

- client storage risklərini ayrıca nəzərə al

## Refresh token saxlama strategiyası

Bu plan üçün seçilmiş yanaşma:

- `access token` response body-də qaytarılır
- `refresh token` yalnız `HttpOnly Secure SameSite` cookie ilə verilir

Səbəb:

- access token frontend tərəfindən bearer header kimi istifadə oluna bilir
- refresh token JavaScript tərəfindən oxunmur
- XSS halında refresh token oğurlanması riski azalır

Diqqət ediləcək məqamlar:

- CSRF qoruması refresh endpoint-ləri üçün ayrıca planlanmalıdır
- cross-site frontend varsa `SameSite=None; Secure` lazım ola bilər
- same-site frontend varsa `SameSite=Strict` və ya `Lax` daha təhlükəsiz ola bilər

## Error handling planı

Standart error response strukturu seç:

- `timestamp`
- `status`
- `error`
- `message`
- `path`
- `traceId`

Tipik auth error-lar:

- invalid credentials
- token expired
- token invalid
- token revoked
- account locked
- account disabled
- email not verified
- access denied

## DTO planı

### Request DTO-lar

- `RegisterRequest`
- `LoginRequest`
- `RefreshTokenRequest`
- `ForgotPasswordRequest`
- `ResetPasswordRequest`
- `VerifyEmailRequest`

### Response DTO-lar

- `AuthResponse`
- `UserProfileResponse`
- `ApiErrorResponse`

`AuthResponse` üçün tövsiyə:

- `accessToken`
- `accessTokenExpiresAt`
- `refreshTokenExpiresAt`
- `tokenType`

Qeyd:

- `refreshToken` body-də verilməsin, cookie ilə yazılsın

## Validation planı

Bean Validation istifadə et:

- `@NotBlank`
- `@Email`
- `@Size`
- custom password validator

Validation həm request səviyyəsində, həm business rule səviyyəsində olmalıdır.

## Observability və audit

Production üçün bunları düşün:

- `traceId` və request correlation
- auth event logging
- suspicious activity monitoring
- failed login dashboard metric-ləri

## Test planı

### Unit test

- password hash verify
- JWT generate/parse
- refresh rotation
- reset token validation

### Integration test

- register success/fail
- login success/fail
- refresh success/reuse/revoked
- forgot password
- reset password
- protected endpoint access
- admin-only endpoint access
- `Set-Cookie` atributlarının yoxlanması
- logout zamanı cookie clear edilməsi

### Security test

- expired token
- forged token
- revoked refresh token
- locked account
- disabled account
- privilege escalation cəhdləri

## Production rollout mərhələləri

### Phase 1

- user entity
- role entity
- register
- login
- access token
- refresh token
- Swagger UI integration

### Phase 2

- forgot password
- reset password
- email verification
- logout
- logout all

### Phase 3

- session management
- audit log
- lock/unlock
- rate limiting
- monitoring

## Minimum Viable Production Scope

Əgər ilk versiyanı balanslı şəkildə çıxarmaq istəsən, minimum production scope belə ola bilər:

- register
- login
- access token
- refresh token rotation
- logout
- forgot password
- reset password
- role-based authorization
- hashed refresh tokens
- email verification
- audit log-un əsas hissəsi
- Swagger UI ilə sənədləşdirilmiş auth endpoint-lər

## Implementasiya sırası

Bu ardıcıllıq daha təhlükəsiz və səliqəlidir:

1. project dependencies əlavə et
2. security config skeleton qur
3. `User`, `Role`, `RefreshToken` entity-lərini yarat
4. Flyway migration-ları yaz
5. password encoder əlavə et
6. `register` implement et
7. `login` implement et
8. JWT `access token` servisini yaz
9. `refresh token` cookie + rotation servisini yaz
10. `JwtAuthenticationFilter` əlavə et
11. protected endpoint test et
12. `logout` və `logout-all` yaz
13. `forgot password` və `reset password` yaz
14. `email verification` əlavə et
15. Swagger UI auth test axınını tamamla
16. audit və rate limiting əlavə et

## Swagger UI planı

Swagger UI aşağıdakılar üçün qurulmalıdır:

- endpoint sənədləşməsi
- request/response nümunələri
- bearer auth ilə qorunan endpoint-lərin test olunması
- login və register flow-larının rahat yoxlanması

Qərar:

- Swagger UI development və test mühiti üçün aktiv olacaq
- production mühitində istəyə görə məhdudlaşdırıla və ya söndürülə bilər

OpenAPI konfiqurasiyasında:

- bearer auth scheme əlavə et
- auth endpoint-ləri aydın qruplaşdır
- cookie-based refresh axınını endpoint description-larda izah et
- `Set-Cookie` davranışını response documentation-da qeyd et

## Son qərar

Bu layihədə production-səviyyəli auth qurmaq üçün əsas qərar budur:

- access token qısaömürlü JWT olacaq
- refresh token uzunömürlü olacaq
- refresh token database-də hash olunmuş formada saxlanacaq
- refresh token `HttpOnly Secure` cookie ilə veriləcək
- hər refresh zamanı token rotate olunacaq
- role-based authorization əvvəlcə sadə saxlanacaq
- password reset və email verification başlanğıcdan planlanacaq
- Swagger UI ilə auth endpoint-lər sənədləşdiriləcək

Bu plan həm öyrənmək, həm də real sistem qurmaq üçün çox yaxşı bazadır.
